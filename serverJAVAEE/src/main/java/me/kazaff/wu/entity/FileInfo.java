package me.kazaff.wu.entity;

/**
 * Created by kazaff on 2014/12/1.
 */
public class FileInfo {

    private String md5;
    private int chunkIndex;
    private String size;
    private String name;
    private String userId;
    private String id;
    private int chunks;
    private int chunk;
    private String lastModifiedDate;
    private String type;
    private String ext;

    public FileInfo(){}

    public FileInfo(String name, String size, int chunkIndex){
        this.name = name;
        this.size = size;
        this.chunkIndex = chunkIndex;
    }

    public FileInfo(String userId, String id){
        this.userId = userId;
        this.id = id;
    }

    public FileInfo(String md5){
        this.md5 = md5;
    }

    public FileInfo(int chunks, int chunk, String userId, String id, String name, String size, String lastModifiedDate, String type){
        this.userId = userId;
        this.id = id;
        this.name = name;
        this.size = size;
        this.chunks = chunks;
        this.chunk = chunk;
        this.lastModifiedDate = lastModifiedDate;
        this.type = type;
    }

    public FileInfo(String name, int chunks, String ext, String md5){
        this.name = name;
        this.chunks = chunks;
        this.ext = ext;
        this.md5 = md5;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public int getChunks() {
        return chunks;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                + "; userId=" + this.userId + "; id=" + this.id + "; chunks=" + this.chunks + "; chunk=" + this.chunk
                + "; lastModifiedDate=" + this.lastModifiedDate + "; type=" + this.type + "; ext=" + this.ext;
    }
}
