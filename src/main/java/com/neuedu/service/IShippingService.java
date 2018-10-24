package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;

public interface IShippingService {

    /**
     * 添加地址
     * */
    ServerResponse add(Integer userid, Shipping shipping);

    /**
     * 删除地址
     * */
    ServerResponse delete(Integer userid,Integer shippingId);
    /**
     * 更新地址
     * */
    ServerResponse update(Integer userid, Shipping shipping);
    /**
     * 查询地址详情
     * */
    ServerResponse select(Integer userid,Integer shippingId);
    /**
     * 分页查询
     * */
    ServerResponse list(Integer userId,Integer pageNo,Integer pageSize);
}
