package com.neuedu.service;

import com.neuedu.common.ServerResponse;

import java.util.Map;

public interface IOrderService {

    /**
     * 创建订单
     * */
    ServerResponse createOrder(Integer userid,Integer shippingid);

    /**
     * 获取商品信息
     * */
    ServerResponse get_order_cart_product(Integer userId);
    /**
     * 前台-订单列表
     * */
    ServerResponse list(Integer userId,Integer pageNum,Integer pageSize);
    /**
     *前台- 获取订单明细
     * */
    ServerResponse detail(Integer userId,Long orderNo);
    /**
     * 前台-取消订单
     * */
    ServerResponse cancel(Integer userId,Long orderNo);
    /**
     * 后台-按照订单号查询
     * */
    ServerResponse search(Long orderNo);
    /**
     * 后台-订单发货
     * */
   ServerResponse  send(Long orderNo);
   /**
    * 前台-下单支付
    * */
  ServerResponse  pay(Integer userId, Long orderNo,String path);
  /**
   * 支付宝回调接口
   * */
  String  alipayCallback(Map<String,String> map);
/**
 * 查询订单状态
 * */
    ServerResponse query_order_pay_status(Long orderNo);
}
