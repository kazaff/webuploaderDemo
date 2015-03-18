package me.kazaff.wu.controller;

import me.kazaff.wu.entity.FileInfo;
import me.kazaff.wu.entity.FileUploadForm;
import me.kazaff.wu.entity.Result;


/**
 * Created by kazaff on 2015/3/18.
 */
public interface FileUploadeService {
    public Result check(String status, FileInfo info);
    public Result fileUpload(FileUploadForm form);
}
