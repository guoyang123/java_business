package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ResponseCode;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 处理后台用户接口
 * */
@RestController
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
     IUserService userService;
    @RequestMapping(value = "/login.do")
    public ServerResponse login(String username,String password,HttpSession session){

      ServerResponse serverResponse= userService.login(username,password);
      UserInfo userInfo=(UserInfo) serverResponse.getData();
      if(userInfo.getRole()!= Const.USERROLE.ADMINUSER){ //防止纵向越权
          //普通用户，没有权限登录后台管理系统
          return  ServerResponse.createByError("普通用户无法访问后台");
      }else{
          session.setAttribute(Const.CURRENT_USER,userInfo);
      }
      return serverResponse;
    }

    /**
     * 分页获取用户
     * */
    @RequestMapping(value = "/list.do")
    public  ServerResponse findUserByPageNo(@RequestParam(required = false,defaultValue = "1") int pageNo,
                                            @RequestParam(required = false,defaultValue = "10") int pageSize,
                                            HttpSession session){

       UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURRENT_USER);
        if(userInfo==null){//未登录或登录过期
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
      //判断是否为管理员
         ServerResponse serverResponse=userService.checkUserAdmin(userInfo);
      if(serverResponse.isSuccess()){//管理员
         return userService.selectUserByPageNo(pageNo,pageSize);
      }else{
          return ServerResponse.createByError("不是管理员，没有权限操作");
      }

    }

}
