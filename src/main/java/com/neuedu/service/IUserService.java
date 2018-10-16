package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;

import javax.servlet.http.HttpSession;

public interface IUserService {

   public  ServerResponse login(String username,String password);
   public ServerResponse register(UserInfo userInfo);
   public  ServerResponse checkValid(String str,String type);
   public ServerResponse forget_get_question(String username);
   public ServerResponse forget_answer(String username,String question,String answer);
    public  ServerResponse forget_reset_password(String username,String passwordNew,String forgetToken);
   public  ServerResponse reset_password(String passwordOld,String passwordNew,UserInfo userInfo);
   public ServerResponse  updateUserInfo(UserInfo user);


   /**
    * 分页查询
    * @param  pageNo 页码(第几页)
    *@param  pageSize 查询数据量
    * */
   public ServerResponse selectUserByPageNo(int pageNo,int pageSize);

   /**
    * 判断是否为管理员
    * */
   public  ServerResponse checkUserAdmin(UserInfo userInfo);


}
