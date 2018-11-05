package com.neuedu.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mysql.fabric.Server;
import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    /**
     * 创建订单
     * */
    @RequestMapping(value = "/create.do")
    public ServerResponse createOrder(HttpSession session,Integer shippingId){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.createOrder(userInfo.getId(),shippingId);
    }

    /**
     * 获取订单的商品信息
     * */
    @RequestMapping(value = "/get_order_cart_product.do")
    public ServerResponse get_order_cart_product(HttpSession session){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.get_order_cart_product(userInfo.getId());
    }

    /**
     * 获取订单列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(required = false,defaultValue = "1")Integer pageNum,
                               @RequestParam(required = false,defaultValue = "10")Integer pageSize){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.list(userInfo.getId(),pageNum,pageSize);
    }
    /**
     * 获取订单详情
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session,
                              Long orderNo){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.detail(userInfo.getId(),orderNo);
    }

    /**
     * 取消订单
     * */
    @RequestMapping(value = "/cancel.do")
    public ServerResponse cancel(HttpSession session,
                                 Long orderNo){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(userInfo.getId(),orderNo);
    }


    /**
     * 下单支付接口
     * */
    @RequestMapping(value = "/pay.do")
    public ServerResponse pay(HttpSession session,
                                 Long orderNo){
        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.pay(userInfo.getId(),orderNo);
    }


    /**
     * 支付宝服务器回调商家服务器接口
     * */
    @RequestMapping(value = "/alipay_callback.do")
    public  String callback(HttpServletRequest request){
        System.out.println("======支付宝服务器回调到了应用服务器=========");

       Map<String,String[]> map= request.getParameterMap();
       Map<String,String> paramMap= Maps.newHashMap();
       for(Iterator<String> iterator=map.keySet().iterator();iterator.hasNext();){
            String key=iterator.next();
            String[] values=(String[])map.get(key);
            String value="";
            //["a","b","c"] ---> string- (a,b,c)
            for(int i=0;i<values.length;i++){
                 value=(i==values.length-1)?value+values[i]:value+values[i]+",";
            }
           paramMap.put(key,value);
       }
        System.out.println(paramMap);
        //支付宝验证签名
        paramMap.remove("sign_type");
        try {
           boolean result= AlipaySignature.rsaCheckV2(paramMap, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
           if(result){
               //验证签名成功，说明支付宝回调了该接口
               return orderService.alipayCallback(paramMap);
           }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return "failed";
    }

    /**
     * 查询订单支付状态
     *
     * */
    @RequestMapping(value ="/query_order_pay_status.do")
    public ServerResponse query_order_pay_status(Long orderNo){

        return orderService.query_order_pay_status(orderNo);
    }

}
