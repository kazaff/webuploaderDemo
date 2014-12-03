package me.kazaff.wu.service;

import me.kazaff.wu.entity.FileInfo;
import me.kazaff.wu.util.fileLock;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

/**
 * Created by kazaff on 2014/12/2.
 */

@Service
@Scope("prototype")
public class webUploader {

    /**
     * 错误详情
     */
    private String msg;

    /**
     * 秒传验证
     * 根据文件的MD5签名判断该文件是否已经存在
     *
     * @param key   文件的md5签名
     * @return  若存在则返回该文件的路径，不存在则返回null
     */
    public String md5Check(String key){

        //todo 模拟去数据库查找
        Map<String, String> data = new HashMap<String, String>();
        data.put("b0201e4d41b2eeefc7d3d355a44c6f5a", "kazaff2.jpg");

        if(data.containsKey(key)){
           return data.get(key);
        }else{
            return null;
        }
    }

    /**
     * 分片验证
     * 验证对应分片文件是否存在，大小是否吻合
     * @param file  分片文件的路径
     * @param size  分片文件的大小
     * @return
     */
    public boolean chunkCheck(String file, Long size){
        //检查目标分片是否存在且完整
        File target = new File(file);
        if(target.isFile() && size == target.length()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 分片合并操作
     * 要点:
     *  > 管道合并: 避免把大文件全部读入内存
     *  > 并发锁: 避免多线程同时触发合并操作
     *  > 清理: 合并清理不再需要的分片文件、文件夹、tmp文件
     * @param folder    分片文件所在的文件夹名称
     * @param ext       合并后的文件后缀名
     * @param chunks    分片总数
     * @param md5       文件签名
     * @param path      合并后的文件所存储的位置
     * @return
     */
    public String chunksMerge(String folder, String ext, int chunks, String md5, String path){

        //合并后的目标文件
        String target;

        //检查是否满足合并条件：分片数量是否足够
        if(chunks == this.getChunksNum(path + "/" + folder)){

            //同步指定合并的对象
            Lock lock = fileLock.getLock(folder);
            lock.lock();
            try{
                //检查是否满足合并条件：分片数量是否足够
                File[] files = this.getChunks(path + "/" +folder);
                if(chunks == files.length){

                    //管道合并
                    for(File file : files){
                        

                        //删除分片
                    }
                    files = null;

                    //todo 将MD5签名和合并后的文件path存入持久层
                    //this.saveMd52FileMap(md5,)

                    //清理：文件夹，tmp文件


                }
            }finally {
                //解锁
                lock.unlock();
                //清理锁对象
                fileLock.removeLock(folder);
            }
        }

        //去持久层查找对应md5签名，直接返回对应path
        target = this.md5Check(md5);
        if(target == null){
            //todo log
            this.setErrorMsg("数据不完整，可能该文件正在合并中");
            return null;
        }

        return target;
    }

    /**
     * 将MD5签名和目标文件path的映射关系存入持久层
     * @param key   md5签名
     * @param file  文件路径
     * @return
     */
    public boolean saveMd52FileMap(String key, String file){
        //todo
        return true;
    }

    /**
     * 为上传的文件创建对应的保存位置
     * 若上传的是分片，则会创建对应的文件夹结构和tmp文件
     * @param info  上传文件的相关信息
     * @param path  文件保存根路径
     * @return
     */
    public File getReadySpace(FileInfo info, String path){

        //创建上传文件所需的文件夹
        if(!this.createFileFolder(path, false)){
            return null;
        }

        String newFileName;	//上传文件的新名称

        //如果是分片上传，则需要为分片创建文件夹
        if (info.getChunks() > 0) {
            newFileName = String.valueOf(info.getChunk());

            String fileFolder = this.md5(info.getUserId() + info.getName() + info.getType() + info.getLastModifiedDate() + info.getSize());
            if(fileFolder == null){
                return null;
            }

            path += "/" + fileFolder;    //文件上传路径更新为指定文件信息签名后的临时文件夹，用于后期合并

            if(!this.createFileFolder(path, true)){
                return null;
            }

        } else {
            //生成随机文件名
            newFileName = this.randomFileName(info.getName());
        }

        return new File(path, newFileName);
    }

    /**
     * 获取指定文件的所有分片
     * @param folder    文件夹路径
     * @return
     */
    private File[] getChunks(String folder){
        File targetFolder = new File(folder);
        return targetFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return false;
                }
                return true;
            }
        });
    }

    /**
     * 获取指定文件的分片数量
     * @param folder    文件夹路径
     * @return
     */
    private int getChunksNum(String folder){

       File[] filesList = this.getChunks(folder);
        return filesList.length;
    }

    /**
     * 创建存放上传的文件的文件夹
     * @param file  文件夹路径
     * @return
     */
    private boolean createFileFolder(String file, boolean hasTmp){

        //创建存放分片文件的临时文件夹
        File tmpFile = new File(file);
        if(!tmpFile.exists()){
            try {
                tmpFile.mkdir();
            }catch(SecurityException ex){
                //todo log
                this.setErrorMsg("无法创建文件夹");
                return false;
            }
        }

        if(hasTmp){
            //创建一个对应的文件，用来记录上传分片文件的修改时间，用于清理长期未完成的垃圾分片
            tmpFile = new File(file + ".tmp");
            if(tmpFile.exists()){
                tmpFile.setLastModified(System.currentTimeMillis());
            }else{
                try{
                    tmpFile.createNewFile();
                }catch(IOException ex){
                    //todo log
                    this.setErrorMsg("无法创建tmp文件");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 为上传的文件生成随机名称
     * @param originalName  文件的原始名称，主要用来获取文件的后缀名
     * @return
     */
    private String randomFileName(String originalName){
        String ext[] = originalName.split("\\.");
        return UUID.randomUUID().toString() + "." + ext[ext.length-1];
    }

    /**
     * MD5签名
     * @param content   要签名的内容
     * @return
     */
    private String md5(String content){
        StringBuffer sb = new StringBuffer();
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] tmpFolder = md5.digest();

            for(int i = 0; i < tmpFolder.length; i++){
                sb.append(Integer.toString((tmpFolder[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        }catch(NoSuchAlgorithmException ex){
            //todo log
            this.setErrorMsg("无法生成文件的MD5签名");
            return null;
        }catch(UnsupportedEncodingException ex){
            //todo log
            this.setErrorMsg("无法生成文件的MD5签名");
            return null;
        }
    }

    /**
     * 记录异常错误信息
     * @param msg   错误详细
     */
    private void setErrorMsg(String msg){
        this.msg = msg;
    }

    /**
     * 获取错误详细
     * @return
     */
    public String getErrorMsg(){
        return this.msg;
    }
}
