package com.neuedu.controller.backend;

import com.neuedu.common.ServerResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class UploadController {

    @RequestMapping(value = "/upload",method = RequestMethod.GET)
    public  String  upload(){

        return "upload"; // 前缀+逻辑视图+后缀    /WEB-INF/jsp/upload.jsp
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(@RequestParam MultipartFile upload){

        if(upload!=null){
            //aaaa.jpg
            String fileName=upload.getOriginalFilename();
            //获取图片的扩展名
          int lastIndex=  fileName.lastIndexOf(".");
          String fileexpand=fileName.substring(lastIndex);
          //获取新的文件名
          String  filenameNew= UUID.randomUUID().toString()+fileexpand;
            File file=new File("D:\\ftpfile",filenameNew);
            try {
                //将内存数据写到磁盘中
                upload.transferTo(file);
                return  ServerResponse.createBySuccess("成功",filenameNew);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        return null;
    }
}
