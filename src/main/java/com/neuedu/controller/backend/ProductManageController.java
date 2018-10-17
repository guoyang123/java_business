package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IProductService;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    IUserService userService;
    @Autowired
    IProductService productService;
    /**
     * 新增或更新产品接口
     * */
    @RequestMapping(value = "/save.do")
    public ServerResponse saveOrUpdate(Product product, HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //是否有管理员权限
        ServerResponse serverResponse=userService.checkUserAdmin(userInfo);
        if(serverResponse.isSuccess()){//有管理员权限
           return productService.saveOrUpdate(product);
        }else{
            return ServerResponse.createByError("用户无权限操作");

        }

    }

    /**
     * 产品上下架
     * */
    @RequestMapping(value = "/set_sale_status.do")
    public ServerResponse set_sale_status(Integer productId,Integer status, HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //是否有管理员权限
        ServerResponse serverResponse=userService.checkUserAdmin(userInfo);
        if(serverResponse.isSuccess()){//有管理员权限
            return productService.set_sale_status(productId,status);
        }else{
            return ServerResponse.createByError("用户无权限操作");

        }

    }

    /**
     * 产品列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(@RequestParam(required = false,defaultValue = "1") Integer pageNo,
                               @RequestParam(required = false,defaultValue = "10")Integer pageSize,
                               HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //是否有管理员权限
        ServerResponse serverResponse=userService.checkUserAdmin(userInfo);
        if(serverResponse.isSuccess()){//有管理员权限
            return productService.findProductByPage(pageNo,pageSize);
        }else{
            return ServerResponse.createByError("用户无权限操作");

        }

    }

    /**
     * 产品详情
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(Integer productId, HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //是否有管理员权限
        ServerResponse serverResponse=userService.checkUserAdmin(userInfo);
        if(serverResponse.isSuccess()){//有管理员权限
            return productService.findProductDetail(productId);
        }else{
            return ServerResponse.createByError("用户无权限操作");

        }

    }

}
