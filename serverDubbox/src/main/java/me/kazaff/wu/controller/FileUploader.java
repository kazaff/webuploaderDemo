package me.kazaff.wu.controller;

import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import me.kazaff.wu.entity.FileInfo;
import me.kazaff.wu.entity.FileUploadForm;
import me.kazaff.wu.entity.Result;
import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by kazaff on 2015/3/18.
 */

@Path("/")
public class FileUploader implements FileUploadeService {

    @POST
    @Path("fileUpload")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public Result check(@FormParam("status") @DefaultValue("") String status,
                             @Form FileInfo info){

        System.out.println("status: " + status);
        System.out.println("file: " + info);

        return new Result();
    }

    @POST
    @Path("fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public Result fileUpload(@MultipartForm FileUploadForm form){


        System.out.println("name: " + form.getName());

        return new Result();
    }
}
