package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IOrderService;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/order")
public class OrderManageController {
    @Autowired
    IUserService userService;
    @Autowired
    IOrderService orderService;
    /**
     * 获取订单列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(required = false,defaultValue = "1")Integer pageNum,
                               @RequestParam(required = false,defaultValue = "10")Integer pageSize){

        return orderService.list(null,pageNum,pageSize);
    }

    /**
     * 按照订单号查询
     * */
    @RequestMapping(value = "/search.do")
    public ServerResponse search(HttpSession session,Long orderNo){

        return orderService.search(orderNo);
    }


    /**
     * 按照订单号查询
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session,Long orderNo){

        return orderService.search(orderNo);
    }

    /**
     * 后台-订单发货
     * */

    /**
     * 按照订单号查询
     * */
    @RequestMapping(value = "/send_goods.do")
    public ServerResponse send(HttpSession session,Long orderNo){

        return orderService.send(orderNo);
    }





}
