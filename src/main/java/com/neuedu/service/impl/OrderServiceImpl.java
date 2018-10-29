package com.neuedu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.*;
import com.neuedu.pojo.*;
import com.neuedu.service.IOrderService;
import com.neuedu.utils.BigDecimalUtil;
import com.neuedu.utils.DateUtil;
import com.neuedu.utils.PropertiesUtil;
import com.neuedu.vo.OrderItemVO;
import com.neuedu.vo.OrderProductVO;
import com.neuedu.vo.OrderVO;
import com.neuedu.vo.ShippingVO;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    ShippingMapper shippingMapper;

    @Override
    public ServerResponse createOrder(Integer userid, Integer shippingid) {

        //step1:参数校验
        if(shippingid==null){
            return  ServerResponse.createByError("地址必须选择");
        }
        //step2:查询购物车中勾选的商品--》List<Cart>
         List<Cart> cartList=  cartMapper.selectCheckedCartByUserid(userid);

        //step3:List<Cart> --->List<OrderItem>
        ServerResponse serverResponse=getCartOrderItem(userid,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        //step4:创建Order并保存订单
            List<OrderItem> orderItemList=(List<OrderItem>)serverResponse.getData();
           BigDecimal orderTotalPrice= getOrderTotalPrice(orderItemList);
         Order order= assembleOrder(userid,shippingid,orderTotalPrice);
         if(order==null){
             return  ServerResponse.createByError("生成订单失败");
         }
        //step5: 批量插入List<OrderItem>
          if(orderItemList==null||orderItemList.size()==0){
             return ServerResponse.createByError("购物车空");
          }
          for(OrderItem orderItem:orderItemList){
             orderItem.setOrderNo(order.getOrderNo());
          }
          //订单明细批量插入mybatis
          orderItemMapper.batchInsertOrderItem(orderItemList);
        //step6:减商品库存
        reduceProductStock(orderItemList);
        //step7:清空已购买的购物车商品
        cleanCart(cartList);
        //step8: 返回OrderVO
       OrderVO orderVO= assembleOrderVO(order,orderItemList);

        return ServerResponse.createBySuccess(orderVO);
    }



    private OrderVO assembleOrderVO(Order order,List<OrderItem> orderItemList){
        OrderVO orderVO=new OrderVO();
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());
        orderVO.setPostage(0);
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getDesc());
        orderVO.setShippingId(order.getShippingId());
       Shipping shipping= shippingMapper.selectByIdAndUserid(order.getShippingId(),order.getUserId());
       if(shipping!=null){
           orderVO.setReceiverName(shipping.getReceiver_name());
           orderVO.setShippingVO(assembleShippingVO(shipping));
       }
       orderVO.setPaymentTime(DateUtil.dateToStr(order.getPaymentTime()));
        orderVO.setSendTime(DateUtil.dateToStr(order.getSendTime()));
        orderVO.setEndTime(DateUtil.dateToStr(order.getEndTime()));
        orderVO.setCreateTime(DateUtil.dateToStr(order.getCreateTime()));
        orderVO.setCloseTime(DateUtil.dateToStr(order.getCloseTime()));
        orderVO.setImageHost((String) PropertiesUtil.getProperty("imagehost"));

        List<OrderItemVO> orderItemVOList=Lists.newArrayList();
       if(orderItemList!=null){
           for(OrderItem orderItem:orderItemList){
               OrderItemVO orderItemVO=assembleOrderItemVO(orderItem);
               orderItemVOList.add(orderItemVO);
           }
       }

        orderVO.setOrderItemVOList(orderItemVOList);

        return  orderVO;
    }

    /**
     * orderItem-->orderItemVO
     * */
    private  OrderItemVO assembleOrderItemVO(OrderItem orderItem){
        OrderItemVO orderItemVO=new OrderItemVO();
        orderItemVO.setOrderNo(orderItem.getOrderNo());
        orderItemVO.setProductId(orderItem.getProductId());
        orderItemVO.setProductName(orderItem.getProductName());
        orderItemVO.setProductImage(orderItem.getProductImage());
        orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVO.setQuantity(orderItem.getQuantity());
        orderItemVO.setTotalPricel(orderItem.getTotalPrice());
        orderItemVO.setCreateTime(DateUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVO;
    }
    /**
     * shipping-->shippingVO
     * */
    private ShippingVO assembleShippingVO(Shipping shipping){
        ShippingVO shippingVO=new ShippingVO();
        shippingVO.setReceiver_address(shipping.getReceiver_address());
        shippingVO.setReceiver_city(shipping.getReceiver_city());
        shippingVO.setReceiver_district(shipping.getReceiver_district());
        shippingVO.setReceiver_mobile(shipping.getReceiver_mobile());
        shippingVO.setReceiver_name(shipping.getReceiver_name());
        shippingVO.setReceiver_mobile(shipping.getReceiver_mobile());
        shippingVO.setReceiver_province(shipping.getReceiver_province());
        shippingVO.setReceiver_zip(shipping.getReceiver_zip());

        return  shippingVO;
    }
    /**
     * 清空购物车
     * */
    private void cleanCart(List<Cart> cartList){
        for(Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 商品减少库存
     * */
    private void  reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem:orderItemList){
           Product product= productMapper.selectByPrimaryKey(orderItem.getProductId());
           product.setStock(product.getStock()-orderItem.getQuantity());
           productMapper.updateByPrimaryKeySelective(product);
        }
    }
    /**
     * 生成订单
     * */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order=new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(Const.OrderStatusEnum.ORDER_UN_PAY.getStatus());
        order.setPostage(0);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.PAY_ON_LINE.getStatus());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //订单支付时间 发货时间
        int result=orderMapper.insert(order);
        if(result>0){
            return order;
        }
        return null;
    }
    /**
     * 生成订单号
     * */
    private Long  generateOrderNo(){
        long currentTime=System.currentTimeMillis();
        return  currentTime+new Random().nextInt(100);
    }

    /**
     * 计算订单的总价格
     * */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal totalPrice=new BigDecimal("0");
        for(OrderItem orderItem:orderItemList){
            totalPrice= BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return  totalPrice;
    }

    /**
     * 将购物车的购物信息转成订单明细信息
     * */
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList= Lists.newArrayList();
        if(cartList==null||cartList.size()==0){
            //购物车空的
            return ServerResponse.createByError("购物车空");
        }
        for(Cart cartItem:cartList){
            OrderItem orderItem=new OrderItem();
            Product product= productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(product==null){
                return  ServerResponse.createByError("商品不存在");
            }
          Integer productStatus=  product.getStatus();
            if(productStatus!= Const.ProductStatusEnum.PRODUCT_ONLLINE.getStatus()){
                //商品下架
                return ServerResponse.createByError("商品"+product.getName()+"已经被下架");
            }
            Integer productStock=product.getStock();
            if(productStock<cartItem.getQuantity()){
                //商品库存不足
                return ServerResponse.createByError("商品"+product.getName()+"库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }


        return ServerResponse.createBySuccess(orderItemList);
    }


    @Override
    public ServerResponse get_order_cart_product(Integer userId) {

        OrderProductVO orderProductVO=new OrderProductVO();

        //购物车获取购物信息
        List<Cart> cartList=   cartMapper.selectCheckedCartByUserid(userId);
       ServerResponse serverResponse= this.getCartOrderItem(userId,cartList);
       if(!serverResponse.isSuccess()){
           return serverResponse;
       }
       List<OrderItem> orderItemList=( List<OrderItem>)serverResponse.getData();
       List<OrderItemVO> orderItemVOList=Lists.newArrayList();
       BigDecimal totalPrice=new BigDecimal("0");
       for(OrderItem orderItem:orderItemList){
           totalPrice=BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
           orderItemVOList.add(assembleOrderItemVO(orderItem));
       }
        orderProductVO.setImageHost((String) PropertiesUtil.getProperty("imagehost"));
       orderProductVO.setOrderItemVOList(orderItemVOList);
       orderProductVO.setProductTotalPrice(totalPrice);

        return ServerResponse.createBySuccess(orderProductVO);
    }

    @Override
    public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=null;
        if(userId==null){
            //管理员
            orderList=orderMapper.selectAll();
        }else{
            orderList=orderMapper.selectAllByUserid(userId);
        }

        List<OrderVO> orderVOList=Lists.newArrayList();
         for(Order order:orderList){
             if(order==null){
                 return ServerResponse.createByError("订单不存在");
             }
             List<OrderItem> orderItemList=orderItemMapper.selectAllByUseridAndOrderNo(userId,order.getOrderNo());
            OrderVO orderVO=assembleOrderVO(order,orderItemList);
             orderVOList.add(orderVO);
         }

        PageInfo pageInfo=new PageInfo(orderVOList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse detail(Integer userId, Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.getOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
      List<OrderItem> orderItemList=  orderItemMapper.selectAllByUseridAndOrderNo(userId,orderNo);
     OrderVO orderVO=   assembleOrderVO(order,orderItemList);

        return ServerResponse.createBySuccess(orderVO);
    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.getOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        if(order.getStatus()!= Const.OrderStatusEnum.ORDER_UN_PAY.getStatus()){
            return  ServerResponse.createByError("已付款订单无法取消");
        }
        Order order1=new Order();
        order1.setId(order.getId());
        order1.setStatus(Const.OrderStatusEnum.ORDER_CANCELLED.getStatus());
       int result= orderMapper.updateOrderBySelectActive(order1);
      if(result>0){
          return ServerResponse.createBySuccess();
      }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse search(Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.selectOrderByOrderNo(orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        List<OrderItem> orderItemList=  orderItemMapper.selectAllByUseridAndOrderNo(order.getUserId(),orderNo);
        OrderVO orderVO=   assembleOrderVO(order,orderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    @Override
    public ServerResponse send(Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.selectOrderByOrderNo(orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        if(order.getStatus()==Const.OrderStatusEnum.ORDER_PAYED.getStatus()){
            order.setStatus(Const.OrderStatusEnum.ORDER_SEND.getStatus());
            order.setSendTime(new Date());
           int result= orderMapper.updateOrderBySelectActive(order);
           if(result>0){
               return ServerResponse.createBySuccess();
           }
        }

        return ServerResponse.createByError();
    }
}

