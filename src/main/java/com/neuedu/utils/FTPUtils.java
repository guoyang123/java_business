package com.neuedu.utils;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtils {

     private static final String  FTPIP=(String)PropertiesUtil.getProperty("ftp.server.ip");
     private static final String  FTPISER=(String)PropertiesUtil.getProperty("ftp.server.username");
     private static final String  FTPPASSWORD=(String)PropertiesUtil.getProperty("ftp.server.password");


     private  String  ftpIp;
     private  int  port;
     private String ftpUser;
     private String ftpPass;

    private   FTPClient ftpClient;
    public FTPUtils() {

    }
    public FTPUtils(String ftpIp, int port,String ftpUser, String ftpPass) {
        this.ftpIp = ftpIp;
        this.ftpUser = ftpUser;
        this.ftpPass = ftpPass;
        this.port=port;
    }



    /**
     * 图片上传
     * */

    public  static boolean  uploadFile(List<File> fileList) throws IOException {

        FTPUtils ftpUtils=new FTPUtils(FTPIP,21,FTPISER,FTPPASSWORD);
        System.out.println("=========开始连接FTP服务器======");
        ftpUtils.uploadFile("img",fileList);
        System.out.println("=========图片上传成功======");
        return false;
    }

    /**
     * @param  remotePath 上传ftp服务器的子目录下
     * @param  files 上传的文件
     * */

    private  boolean uploadFile(String remotePath,List<File> files) throws IOException {

        FileInputStream fis=null;
        //连接ftp服务器
        if(connectServer(this.ftpIp,this.ftpUser,this.ftpPass)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();//打开被动传输
                for(File file:files){
                    fis=new FileInputStream(file);
                    ftpClient.storeFile(file.getName(),fis);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return  false;
    }

   private boolean  connectServer(String ftpIp,String ftpUser,String ftpPass){

     ftpClient=new FTPClient();
       try {
           ftpClient.connect(ftpIp);
           return ftpClient.login(ftpUser,ftpPass);
       } catch (IOException e) {
           e.printStackTrace();
           System.out.println("===连接ftp服务器异常====");
       }
       return false;

   }


    public String getFtpIp() {
        return ftpIp;
    }

    public void setFtpIp(String ftpIp) {
        this.ftpIp = ftpIp;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpPass() {
        return ftpPass;
    }

    public void setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
    }
}
