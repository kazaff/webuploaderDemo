package me.kazaff.wu.entity;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;

/**
 * Created by kazaff on 2014/12/1.
 */
public class FileInfo {

    @FormParam("md5")
    @DefaultValue("")
    private String md5;

    @FormParam("chunkIndex")
    @DefaultValue("-1")
    private int chunkIndex;

    @FormParam("size")
    @DefaultValue("-1")
    private String size;

    @FormParam("name")
    @DefaultValue("")
    private String name;


    @FormParam("chunks")
    @DefaultValue("-1")
    private int chunks;

    @FormParam("ext")
    @DefaultValue("")
    private String ext;

    public FileInfo(){}

    public int getChunks() {
        return chunks;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String toString(){
        return "name=" + this.name + "; size=" + this.size + "; chunkIndex=" + this.chunkIndex + "; md5=" + this.md5
                + "; chunks=" + this.chunks + "; ext=" + this.ext;
    }
}
