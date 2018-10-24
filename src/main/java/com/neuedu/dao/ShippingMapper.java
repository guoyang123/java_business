package com.neuedu.dao;

import com.neuedu.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {

    /**
     * 添加地址
     * */
    int  add(Shipping shipping);
    /**
     * 删除地址
     * */
    int deleteByPrimaryKey(@Param("shippingId") Integer shippingId,
                           @Param("userId") Integer userId);


    /**
     * 更新地址
     * */
    int  update(Shipping shipping);

    /**
     * 查询地址详情
     * */
    Shipping selectByIdAndUserid(@Param("shippingId")Integer shippingId
    ,@Param("userId")Integer userId);

    /**
     * 查询全部
     * */
    List<Shipping> selectAll(Integer userId);

}
