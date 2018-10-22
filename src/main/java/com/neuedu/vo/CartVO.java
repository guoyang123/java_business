package com.neuedu.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
/**
 * 返回到前端的购物车模型对象
 * */
public class CartVO  implements Serializable{

    private List<CartProductVO>  cartProductVOList;
    private BigDecimal cartTotalPrice; //购物车中商品总价格
    private boolean allChecked;//是否全选
    private String imageHost;//图片访问前缀


    public List<CartProductVO> getCartProductVOList() {
        return cartProductVOList;
    }

    public void setCartProductVOList(List<CartProductVO> cartProductVOList) {
        this.cartProductVOList = cartProductVOList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public boolean isAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
