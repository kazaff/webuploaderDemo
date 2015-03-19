package me.kazaff.wu.controller;

import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import me.kazaff.wu.entity.FileInfo;
import me.kazaff.wu.entity.FileUploadForm;
import me.kazaff.wu.entity.Result;
import me.kazaff.wu.service.webUploader;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by kazaff on 2015/3/18.
 */

@Path("/")
public class FileUploader implements FileUploadeService {

    private final static Logger log = LoggerFactory.getLogger(FileUploader.class);

    @Value("${upload.folder}")
    private String uploadFolder;

    @Autowired
    private webUploader wu;

    @POST
    @Path("fileUpload")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED+";charset=UTF-8")
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public Result check(@FormParam("status") @DefaultValue("") String status, @Form FileInfo info){

        Result rs = new Result();

        //秒传验证
        if("md5Check".equals(status)){
            String path = wu.md5Check(info.getMd5());
            if(path == null || path.isEmpty()){
                rs.setIfExist(false);
            }else{
                rs.setIfExist(true);
                rs.setPath(path);
            }

        }else if("chunkCheck".equals(status)){  //分块验证

            //检查目标分片是否存在且完整
            if(wu.chunkCheck(this.uploadFolder + "/" + info.getName() + "/" + info.getChunkIndex(), Long.valueOf(info.getSize()))){
                rs.setIfExist(true);
            }else{
                rs.setIfExist(false);
            }

        }else if("chunksMerge".equals(status)){ //分块合并

            String path = wu.chunksMerge(info.getName(), info.getExt(), info.getChunks(), info.getMd5(), this.uploadFolder);
            if(path == null){
                rs.setStatus(0);
                rs.setMessage(wu.getErrorMsg());
            }else{
                rs.setStatus(1);
                rs.setPath(path);
            }

        }else{
            log.error("请求参数不完整");
            rs.setStatus(0);
            rs.setMessage("请求参数不完整");
        }

        return rs;
    }

    @POST
    @Path("fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public Result fileUpload(MultipartFormDataInput formDataInput){

        //转换成实体对象
        FileUploadForm form = new FileUploadForm();
        Map<String, List<InputPart>> uploadForm = formDataInput.getFormDataMap();

        try{
            //name
            InputPart inputParts = uploadForm.get("name").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setName(inputParts.getBodyAsString());

            //lastModifiedDate
            inputParts = uploadForm.get("lastModifiedDate").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setLastModifiedDate(inputParts.getBodyAsString());

            //userId
            inputParts = uploadForm.get("userId").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setUserId(inputParts.getBodyAsString());

            //md5
            inputParts = uploadForm.get("md5").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setMd5(inputParts.getBodyAsString());

            //id
            inputParts = uploadForm.get("id").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setId(inputParts.getBodyAsString());

            //type
            inputParts = uploadForm.get("type").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setType(inputParts.getBodyAsString());

            //size
            inputParts = uploadForm.get("size").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setSize(inputParts.getBodyAsString());

            //chunks
            inputParts = uploadForm.get("chunks").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setChunks(Integer.valueOf(inputParts.getBodyAsString()));

            //chunk
            inputParts = uploadForm.get("chunk").get(0);
            inputParts.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            form.setChunk(Integer.valueOf(inputParts.getBodyAsString()));

            //file
            inputParts = uploadForm.get("file").get(0);
            InputStream inputStream = inputParts.getBody(InputStream.class, null);
            form.setFileData(IOUtils.toByteArray(inputStream));


        }catch (IOException e) {
            log.error("异常: " + e);
            return new Result(0, "数据上传失败");
        }

        System.out.println(form);

        return handleUpload(form);
    }

    /*
    //由于能力有限，不知道如何在使用@MultipartForm时，解决中文乱码问题
    @POST
    @Path("fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA+";charset=UTF-8")
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public Result fileUpload(@MultipartForm FileUploadForm form){
        return handleUpload(form);
    }*/

    private Result handleUpload(FileUploadForm form){
        try{
            File target = wu.getReadySpace(form, this.uploadFolder);
            if(target == null) {
                return new Result(0, wu.getErrorMsg());
            }

            //todo 可以使用buffer或nio通道来优化文件读写
            FileOutputStream out = new FileOutputStream(target);
            out.write(form.getFileData());
            out.flush();
            out.close();

            //将MD5签名和合并后的文件path存入持久层，注意这里这个需求导致需要修改webuploader.js源码3170行
            //因为原始webuploader.js不支持为formData设置函数类型参数，这将导致不能在控件初始化后修改该参数
            if(form.getChunks() <= 0){
                if(!wu.saveMd52FileMap(form.getMd5(), target.getName())){
                    log.error("文件[" + form.getMd5() + "=>" + target.getName() + "]保存关系到持久成失败，但并不影响文件上传，只会导致日后该文件可能被重复上传而已");
                }
            }

            Result rs = new Result();
            rs.setStatus(1);
            rs.setPath(target.getName());
            return rs;

        }catch (IOException ex){
            log.error("数据上传失败", ex);
            return new Result(0, "数据上传失败");
        }
    }
}
