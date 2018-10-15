package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理后台用户接口
 * */
@RestController
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
     IUserService userService;
    @RequestMapping(value = "/login.do")
    public ServerResponse login(String username,String password){

      ServerResponse serverResponse= userService.login(username,password);
      UserInfo userInfo=(UserInfo) serverResponse.getData();
      if(userInfo.getRole()!= Const.USERROLE.ADMINUSER){ //防止纵向越权
          //普通用户，没有权限登录后台管理系统
          return  ServerResponse.createByError("普通用户无法访问后台");
      }
      return serverResponse;
    }

}
