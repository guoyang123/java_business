package com.neuedu.service;

import com.mysql.fabric.Server;
import com.neuedu.common.ServerResponse;

public interface ICartService {


    /**
     * 商品添加到购物车
     * */
    ServerResponse add(Integer userid,Integer productId,Integer count);
    /**
     * 获取购物车列表
     * */
    ServerResponse list(Integer userid);
    /**
     * 更新购物车商品
     * */
  ServerResponse  update(Integer userid,Integer productId,Integer count);
  /**
   * 删除购物车商品
   * @param  productIds 1,2,3
   * */
 ServerResponse delete(Integer userid,String productIds);

 /**
  * @param  userid
  * @param checked  1:选中 0：取消选中
  * @Param productId productId=null 全选/全不选
  *         productId！=null  单选/取消单选
  * */
 ServerResponse selectOrUnselect(Integer userid,Integer productId, Integer checked);

    ServerResponse<Integer> get_cart_product_count(Integer userid);
}
