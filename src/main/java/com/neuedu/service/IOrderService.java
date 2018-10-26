package com.neuedu.service;

import com.neuedu.common.ServerResponse;

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
}
