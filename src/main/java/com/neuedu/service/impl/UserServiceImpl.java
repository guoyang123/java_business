package com.neuedu.service.impl;

import com.neuedu.common.ServerResponse;
import com.neuedu.dao.UserInfoMapper;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public ServerResponse login(String username, String password) {

        //step1:校验用户名
        int result=userInfoMapper.checkUsername(username);

        //step2:登录操作
        if(result>0){
            //todo  MD5(passsord)

            //存在用户
         UserInfo userInfo= userInfoMapper.selectLogin(username,password);
          if(userInfo==null){//密码错误
              return ServerResponse.createByError("密码错误");
          }else{
              userInfo.setPassword("");
              return ServerResponse.createBySuccess("success",userInfo);
          }
        }else{//用户名不存在
             return ServerResponse.createByError("用户名不存在");
        }
    }
}
