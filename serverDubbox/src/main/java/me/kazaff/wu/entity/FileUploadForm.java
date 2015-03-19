package me.kazaff.wu.entity;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

/**
 * Created by kazaff on 2015/3/18.
 */
public class FileUploadForm {

    private byte[] fileData;
    private String userId;
    private String md5;
    private String id;
    private String name;
    private String type;
    private String lastModifiedDate;
    private String size;
    private int chunks;
    private int chunk;

    public int getChunks() {
        return chunks;
    }

    @FormParam("chunks")
    @DefaultValue("-1")
    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public int getChunk() {
        return chunk;
    }

    @FormParam("chunk")
    @DefaultValue("-1")
    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public String getUserId() {
        return userId;
    }

    @FormParam("userId")
    @DefaultValue("-1")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMd5() {
        return md5;
    }

    @FormParam("md5")
    @DefaultValue("-1")
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getId() {
        return id;
    }

    @FormParam("id")
    @DefaultValue("-1")
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @FormParam("name")
    @DefaultValue("")
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    @FormParam("type")
    @DefaultValue("")
    public void setType(String type) {
        this.type = type;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    @FormParam("lastModifiedDate")
    @DefaultValue("")
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getSize() {
        return size;
    }

    @FormParam("size")
    @DefaultValue("-1")
    public void setSize(String size) {
        this.size = size;
    }

    public byte[] getFileData() {
        return fileData;
    }

    @FormParam("file")
    @DefaultValue("")
    @PartType("application/octet-stream")
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    @Override
    public String toString() {
        return "FileUploadForm{" +
                ", userId='" + userId + '\'' +
                ", md5='" + md5 + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", size='" + size + '\'' +
                ", chunks=" + chunks +
                ", chunk=" + chunk +
                '}';
    }
}
