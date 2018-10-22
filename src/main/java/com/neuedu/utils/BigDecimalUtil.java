package com.neuedu.utils;

import java.math.BigDecimal;

/**
 * 价格运算工具类
 *
 * */
public class BigDecimalUtil {

 /**
  * 加法
  * */

 public  static BigDecimal add(Double v1,Double v2){
     BigDecimal bigDecimal=new BigDecimal(v1.toString());
     BigDecimal bigDecimal2=new BigDecimal(v2.toString());
     return  bigDecimal.add(bigDecimal2);
 }

 /**
  * 减法
  * */
 public  static  BigDecimal  sub(Double v1,Double v2){
     BigDecimal bigDecimal=new BigDecimal(v1.toString());
     BigDecimal bigDecimal2=new BigDecimal(v2.toString());
     return  bigDecimal.subtract(bigDecimal2);
 }
 /**
  * 乘法
  * */
 public  static  BigDecimal  mul(Double v1,Double v2){
     BigDecimal bigDecimal=new BigDecimal(v1.toString());
     BigDecimal bigDecimal2=new BigDecimal(v2.toString());
     return  bigDecimal.multiply(bigDecimal2);
 }

 /**
  * 除法
  * */
 public  static  BigDecimal  divi(Double v1,Double v2){
     BigDecimal bigDecimal=new BigDecimal(v1.toString());
     BigDecimal bigDecimal2=new BigDecimal(v2.toString());
     // 10.0/3 --> 3.33
     return  bigDecimal.divide(bigDecimal2,2,BigDecimal.ROUND_HALF_UP);
 }



}
