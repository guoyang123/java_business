package com.neuedu.vo;

import com.neuedu.common.ServerResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class OrderProductVO  implements Serializable{

    private BigDecimal productTotalPrice;
    private  String imageHost;
    private List<OrderItemVO> orderItemVOList;

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public List<OrderItemVO> getOrderItemVOList() {
        return orderItemVOList;
    }

    public void setOrderItemVOList(List<OrderItemVO> orderItemVOList) {
        this.orderItemVOList = orderItemVOList;
    }
}
