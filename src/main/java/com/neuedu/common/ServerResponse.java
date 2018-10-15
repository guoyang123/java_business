package com.neuedu.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable{

    //状态码
    private   int  status;
   //数据
     private T  data;  //
     //提示信息
    private  String msg;

 private  ServerResponse(int status){
     this.status=status;
 }
 private ServerResponse(int status,String msg){
     this.status=status;
     this.msg=msg;
 }
 private  ServerResponse(int status ,T data){
     this.status=status;
     this.data=data;
 }
 private  ServerResponse(int status,T data,String msg){
     this.status=status;
     this.data=data;
     this.msg=msg;
 }

 //判断接口返回数据是否成功
    @JsonIgnore
    //不在json序列化对象中
    public  boolean isSuccess(){
     return  this.status==ResponseCode.SUCCESS.getCode();
    }

    public  static <T> ServerResponse<T> createBySuccess(){
     return  new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public  static <T> ServerResponse<T> createBySuccess(String msg){
      return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
      public static <T> ServerResponse<T> createBySuccess(T data){
         return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
      }
    public static <T> ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data,msg);
    }
    public static <T> ServerResponse<T> createByError(){

     return new ServerResponse<T>(ResponseCode.ERROR.getCode());
    }
    public static <T> ServerResponse<T> createByError(String msg){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),msg);
    }
    public static <T> ServerResponse<T> createByError(int status,String msg){
        return new ServerResponse<T>(status,msg);
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }
}