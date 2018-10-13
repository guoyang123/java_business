package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;

public interface IUserService {

   public  ServerResponse login(String username,String password);
   public ServerResponse register(UserInfo userInfo);
   public  ServerResponse checkValid(String str,String type);
   public ServerResponse forget_get_question(String username);
   public ServerResponse forget_answer(String username,String question,String answer);
    public  ServerResponse forget_reset_password(String username,String passwordNew,String forgetToken);
}
