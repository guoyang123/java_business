package com.neuedu.service;


import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;
import org.springframework.web.bind.annotation.RequestParam;

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
    /**
     * 后台搜索商品
     * @param  productId
     * @param  productName
     * @param  pageNo
     * @param  pageSize
     * */
    ServerResponse searchProductsByProductIdOrProductName(Integer productId,String productName,Integer pageNo,Integer pageSize);


    /**
     * 前台商品搜索接口
     * */
    ServerResponse searchProduct( String keyword, Integer categoryId, Integer pageNo, Integer pageSize, String orderBy);

}
