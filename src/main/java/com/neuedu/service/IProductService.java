package com.neuedu.service;


import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;

public interface IProductService {
 /**
  * 更新或者添加商品
  * */
 ServerResponse saveOrUpdate(Product product);

    /***
     *
     *产品上下架接口
     */

    ServerResponse set_sale_status(Integer productId,Integer status);

    /**
     * 分页查询商品数据
     * */
    ServerResponse findProductByPage(Integer pageNo,Integer pageSize);

    /**
     * 产品详情接口
     * */
    ServerResponse findProductDetail(Integer productId);

}
