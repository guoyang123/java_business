package com.neuedu.utils;

import com.neuedu.common.RedisPool;
import redis.clients.jedis.Jedis;

/**
 * 封装了reids常用api
 * */
public class RedisPoolUtil {



    /**
     * 设置key的有效期，单位秒
     * */
    public static  Long expire(String key,int exTime){

        Long result=null;
        Jedis jedis=null;
        try{
            jedis=RedisPool.getJedis();
            result=jedis.expire(key, exTime);
        }catch (Exception e){
            e.printStackTrace();
            RedisPool.returnBrokenJedis(jedis);
            return  result;
        }
        RedisPool.returnJedis(jedis);
        return  result;

    }


    /**
     * set
     * */
    public static  String set(String key,String value){

        String result=null;
        Jedis jedis=null;
        try{
             jedis=RedisPool.getJedis();
            result=jedis.set(key, value);
        }catch (Exception e){
             e.printStackTrace();
             RedisPool.returnBrokenJedis(jedis);
             return  result;
        }
        RedisPool.returnJedis(jedis);
        return  result;

    }

    /**
     * setex
     * */
    public static  String setEx(String key,String value,int exTime){

        String result=null;
        Jedis jedis=null;
        try{
            jedis=RedisPool.getJedis();
            result=jedis.setex(key,exTime ,value);
        }catch (Exception e){
            e.printStackTrace();
            RedisPool.returnBrokenJedis(jedis);
            return  result;
        }
        RedisPool.returnJedis(jedis);
        return  result;

    }

    /**
     * get
     * */
    public static  String get(String key){

        String result=null;
        Jedis jedis=null;
        try{
            jedis=RedisPool.getJedis();
            result=jedis.get(key);
        }catch (Exception e){
            e.printStackTrace();
            RedisPool.returnBrokenJedis(jedis);
            return  result;
        }
        RedisPool.returnJedis(jedis);
        return  result;

    }

    /**
     * delete
     * */
    public static  Long del(String key){

        Long result=null;
        Jedis jedis=null;
        try{
            jedis=RedisPool.getJedis();
            result=jedis.del(key);
        }catch (Exception e){
            e.printStackTrace();
            RedisPool.returnBrokenJedis(jedis);
            return  result;
        }
        RedisPool.returnJedis(jedis);
        return  result;

    }

    public static void main(String[] args) {
        System.out.println(get("jedis"));
        System.out.println(setEx("a1","aaa",10));
        System.out.println(del("jedis"));
    }
}
