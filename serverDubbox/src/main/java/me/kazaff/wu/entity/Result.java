package me.kazaff.wu.entity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kazaff on 2015/3/18.
 */
@XmlRootElement
public class Result {

    private int status;
    private String message;
    private String path;
    private boolean ifExist;

    public Result() {
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Result(int status, String message, String path, boolean ifExist) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.ifExist = ifExist;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIfExist() {
        return ifExist;
    }

    public void setIfExist(boolean ifExist) {
        this.ifExist = ifExist;
    }
}
