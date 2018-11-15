package com.neuedu.controller.portal;

import com.neuedu.common.ServerResponse;
import com.neuedu.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/product")
public class ProductController {

    @Autowired
    IProductService productService;
    /**
     * 产品搜索
     *
     * */

    @RequestMapping(value = "/list/keyword/{keyword}/{pageNo}/{pageSize}/{orderBy}")
    public ServerResponse listByKeyword(@PathVariable("keyword") String keyword,
                              @PathVariable("pageNo") Integer pageNo,
                               @PathVariable("pageSize") Integer pageSize,
                               @PathVariable("orderBy") String orderBy){

        return productService.searchProduct(keyword,null,pageNo,pageSize,orderBy);
    }
    @RequestMapping(value = "/list/categoryId/{categoryId}/{pageNo}/{pageSize}/{orderBy}")
    public ServerResponse listByCategoryId(
                               @PathVariable("categoryId") Integer categoryId,
                               @PathVariable("pageNo") Integer pageNo,
                               @PathVariable("pageSize") Integer pageSize,
                               @PathVariable("orderBy") String orderBy){


        return productService.searchProduct(null,categoryId,pageNo,pageSize,orderBy);
    }
    @RequestMapping(value = "/detail/{productId}")
    public  ServerResponse productDeatail(@PathVariable("productId") Integer productId){

        //int a=3/0;

        return productService.productDeatail(productId);
    }
}
