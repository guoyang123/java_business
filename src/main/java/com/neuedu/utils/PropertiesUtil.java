package com.neuedu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * 操作properties文件的工具类
 * */
public class PropertiesUtil {

    private static Properties properties=new Properties();
    static {

        InputStream inputStream=Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  static  Object  getProperty(String key){
       return  properties.get(key);
    }


}
