package com.neuedu.controller.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.neuedu.common.ServerResponse;
import com.neuedu.utils.FTPUtils;
import com.neuedu.utils.PropertiesUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Controller
public class UploadController {

    @RequestMapping(value = "/upload",method = RequestMethod.GET)
    public  String  upload(){

        return "upload"; // 前缀+逻辑视图+后缀    /WEB-INF/jsp/upload.jsp
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpServletRequest request,@RequestParam MultipartFile upload){

        if(upload!=null){
            //aaaa.jpg
            String fileName=upload.getOriginalFilename();
            //获取图片的扩展名
          int lastIndex=  fileName.lastIndexOf(".");
          String fileexpand=fileName.substring(lastIndex);
          //获取新的文件名
          String  filenameNew= UUID.randomUUID().toString()+fileexpand;
         String uploadPath=  request.getServletContext().getRealPath("upload");
          File uploadFile=new File(uploadPath);
          if(!uploadFile.exists()){
              uploadFile.mkdirs();
          }
            File file=new File(uploadPath,filenameNew);
            try {
                //将内存数据写到磁盘中
                upload.transferTo(file);
                //图片上传到ftp服务器
                FTPUtils.uploadFile(Lists.newArrayList(file));
                //删除掉应用服务器上的图片
                file.delete();

                Map<String,String> map= Maps.newHashMap();
                map.put("uri",filenameNew);
                map.put("url", (String) PropertiesUtil.getProperty("imagehost")+filenameNew);

                return  ServerResponse.createBySuccess("成功",map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        return ServerResponse.createByError("图片上传失败");
    }
}
