package com.neuedu.controller.portal;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    IUserService userService;

 @RequestMapping(value = "/login.do")
    public ServerResponse login(String username, String password, HttpSession session){

      ServerResponse serverResponse= userService.login(username,password);
      if(serverResponse.isSuccess()){//登录成功
          session.setAttribute(Const.CURRENT_USER,serverResponse.getData());
      }

     return serverResponse;
     }

     @RequestMapping(value = "/register.do",method = RequestMethod.POST)
    public  ServerResponse register(UserInfo userInfo){

     return  userService.register(userInfo);

    }

    @RequestMapping(value = "/check_valid.do")
   public  ServerResponse checkValid(String  str,String type){
     return userService.checkValid(str,type);
   }

   /**获取登录状态下用户信息*/

   @RequestMapping(value = "/get_user_info.do")
   public  ServerResponse getUserInfo(HttpSession session){

       Object o=session.getAttribute(Const.CURRENT_USER);
       if(o==null){//用户未登录
           return ServerResponse.createByError("用户未登录或者已过期");
       }
       return ServerResponse.createBySuccess("成功",o);
   }


   /**
    * 忘记密码--获取密保问题
    * */
   @RequestMapping(value = "/forget_get_question.do")
   public  ServerResponse forget_get_question(String username){

       return  userService.forget_get_question(username);
   }

   /**
    * 提交问题答案
    * */
   @RequestMapping(value = "/forget_check_answer.do")
  public  ServerResponse forget_answer(String username,String question,String answer){


      return userService.forget_answer(username,question,answer);
  }
/**
 * 忘记密码之重设密码
 * */
@RequestMapping(value = "/forget_reset_password.do")
public  ServerResponse forget_reset_password(String username,String passwordNew,String forgetToken){
    return userService.forget_reset_password(username, passwordNew, forgetToken);
}


}
