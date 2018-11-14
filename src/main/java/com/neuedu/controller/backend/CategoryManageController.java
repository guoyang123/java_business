package com.neuedu.controller.backend;

import com.mysql.fabric.Server;
import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.ICategoryService;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ICategoryService categoryService;
    /**
     * 添加节点
     * */
    @RequestMapping(value = "/add_category.do")
    public ServerResponse add_category(@RequestParam(required = false,defaultValue = "0")int parentId,
                                       String categoryName,
                                       HttpSession session){

        return categoryService.addCategory(parentId,categoryName);

    }


    /**
     * 获取子节点
     * */
    @RequestMapping(value ="/get_category.do")
    public  ServerResponse  get_category(int categoryId,HttpSession session){
        return categoryService.getCategory(categoryId);
    }
    /**
     * 修改子节点
     * */
    @RequestMapping(value ="/set_category_name.do")
    public  ServerResponse  set_category_name(Integer categoryId,String categoryName,HttpSession session){

        return categoryService.set_category_name(categoryId,categoryName);
    }
    /**
     * 递归查询后代节点
     * */
    @RequestMapping(value ="/get_deep_category.do")
    public  ServerResponse  get_deep_category(Integer categoryId,HttpSession session){

        return categoryService.get_deep_category(categoryId);
    }


}
