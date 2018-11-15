package com.neuedu.controller.common.scheduler;

import com.neuedu.service.IOrderService;
import com.neuedu.utils.DateUtil;
import com.neuedu.utils.PropertiesUtil;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 定时关单扫描类
 * */
@Component
public class CloseOrder {

    @Autowired
  IOrderService orderService;

    //定时任务调度方法，每隔一秒钟执行一次该方法
    @Scheduled(cron = "0 0/1 * * * *")
    public  void  closeOrder(){

        System.out.println("=========执行定时任务调度====");

        //去查询数据库中超过过期时间并且未支付的订单，关闭这些未支付的订单

       Integer hour=Integer.parseInt((String) PropertiesUtil.getProperty("close.order.hour"));
       //关闭hour之前的订单
       String closeOrderTime=DateUtil.dateToStr( DateUtils.addHours(new Date(),-hour));

      orderService.closeOrder(closeOrderTime);
    }

}
