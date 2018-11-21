package com.neuedu.common;

import com.neuedu.utils.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis连接池封装类
 * */
public class RedisPool {

    private static  JedisPool jedisPool;
 private  static String redisIp= (String) PropertiesUtil.getProperty("redis.ip");
    private  static String redisPass= (String) PropertiesUtil.getProperty("redis.password");
 private static Integer redisPort= (Integer.parseInt((String)PropertiesUtil.getProperty("redis.port")));
 private static  Integer maxTotal= (Integer.parseInt((String)PropertiesUtil.getProperty("redis.max.total")));
 private static  Integer maxIdle= (Integer.parseInt((String)PropertiesUtil.getProperty("redis.max.idle")));
 private static  Integer minIdle= (Integer.parseInt((String)PropertiesUtil.getProperty("redis.min.idle")));

 private static  Boolean testBorrow=(Boolean.parseBoolean((String)PropertiesUtil.getProperty("redis.test.borrow")));
 private static  Boolean testReturn=(Boolean.parseBoolean((String)PropertiesUtil.getProperty("redis.test.return")));


   private  static void initPool(){
       JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
       jedisPoolConfig.setMaxTotal(maxTotal);
       jedisPoolConfig.setMaxIdle(maxIdle);
       jedisPoolConfig.setMinIdle(minIdle);
       jedisPoolConfig.setTestOnBorrow(testBorrow);
       jedisPoolConfig.setTestOnReturn(testReturn);
       //当连接池中的连接耗尽时，如果设置false:报错，抛出异常；true:阻塞，知道超时
       jedisPoolConfig.setBlockWhenExhausted(true);


        jedisPool=new JedisPool(jedisPoolConfig,redisIp,redisPort,1000*2);//,redisPass

   }

   static {
       initPool();
   }

   /**
    * 获取jedis实例
    * */
  public  static Jedis getJedis(){
      return  jedisPool.getResource();
  }
  /**
   * 将jedis放回到连接池
   * */
  public  static void returnJedis(Jedis jedis){

      jedisPool.returnResource(jedis);
  }
    /**
     * 将jedis放回到连接池
     * */
    public  static void returnBrokenJedis(Jedis jedis){

        jedisPool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        System.out.println("==========start===");
        Jedis jedis=getJedis();
        jedis.set("jedis","testjedis");
        returnJedis(jedis);
        jedisPool.destroy();
        System.out.println("==========end===");
    }


}
