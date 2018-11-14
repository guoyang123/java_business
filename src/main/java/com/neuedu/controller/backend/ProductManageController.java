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

        return productService.saveOrUpdate(product);

    }

    /**
     * 产品上下架
     * */
    @RequestMapping(value = "/set_sale_status.do")
    public ServerResponse set_sale_status(Integer productId,Integer status, HttpSession session){

        return productService.set_sale_status(productId,status);

    }

    /**
     * 产品列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(@RequestParam(required = false,defaultValue = "1") Integer pageNo,
                               @RequestParam(required = false,defaultValue = "10")Integer pageSize,
                               HttpSession session){

        return productService.findProductByPage(pageNo,pageSize);

    }

    /**
     * 产品详情
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(Integer productId, HttpSession session){

        return productService.findProductDetail(productId);

    }

    /**
     * 产品搜索
     * */
    @RequestMapping(value = "/search.do")
    public ServerResponse search(@RequestParam(required = false) Integer productId,
                                 @RequestParam(required = false)String productName,
                                 @RequestParam(required = false,defaultValue = "1") Integer pageNo,
                                 @RequestParam(required = false,defaultValue = "10") Integer pageSize,
                                 HttpSession session){

        return productService.searchProductsByProductIdOrProductName(productId,productName,pageNo,pageSize);


    }


}
