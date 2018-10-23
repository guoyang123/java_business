package com.neuedu.controller.portal;

import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/cart")
public class CartController {
@Autowired
private ICartService cartService;

/**
 * 购物车添加商品
 * */
@RequestMapping(value = "/add.do")
public ServerResponse add(HttpSession session,Integer productId,Integer count){

    //用户是否登录
    UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
    if(userInfo==null){//需要登录
        return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }

    return cartService.add(userInfo.getId(),productId,count);
}


    /**购物车List列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.list(userInfo.getId());
    }


    /**
     * 更新购物车某个产品数量
     * */
    @RequestMapping(value = "/update.do")
    public ServerResponse update(HttpSession session,Integer productId,Integer count){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.update(userInfo.getId(),productId,count);
    }

    /**
     * 移除购物车某个产品
     * */
    @RequestMapping(value = "/delete.do")
    public ServerResponse update(HttpSession session,String productIds){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return cartService.delete(userInfo.getId(),productIds);
    }

    /**
     * 购物车全选
     * */
    @RequestMapping(value = "/select_all.do")
    public ServerResponse select_all(HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

      //  return cartService.delete();
        return cartService.selectOrUnselect(userInfo.getId(),null,Const.Cart.CHECKED);
    }
    /**
     * 购物车取消全选
     * */
    @RequestMapping(value = "/un_select_all.do")
    public ServerResponse un_select_all(HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        //  return cartService.delete();
        return cartService.selectOrUnselect(userInfo.getId(),null,Const.Cart.UNCHECKED);
    }
    /**
     * 购物单选
     * */
    @RequestMapping(value = "/select.do")
    public ServerResponse select_all(HttpSession session,Integer productId){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        //  return cartService.delete();
        return cartService.selectOrUnselect(userInfo.getId(),productId,Const.Cart.CHECKED);
    }
    /**
     * 购物车取消单选
     * */
    @RequestMapping(value = "/un_select.do")
    public ServerResponse un_select_all(HttpSession session,Integer productId){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        //  return cartService.delete();
        return cartService.selectOrUnselect(userInfo.getId(),productId,Const.Cart.UNCHECKED);
    }

    /**
     * 查询在购物车里的产品数量
     * */
    @RequestMapping(value = "/get_cart_product_count.do")
    public ServerResponse<Integer> get_cart_product_count(HttpSession session){

        //用户是否登录
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//需要登录
            return ServerResponse.createBySuccess(0);
        }

        //  return cartService.delete();
        return cartService.get_cart_product_count(userInfo.getId());
    }

}
