package com.neuedu.controller;

import com.neuedu.common.ServerResponse;
import com.neuedu.dao.UserInfoMapper;
import com.neuedu.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/user")
public class UserController {


    @Autowired
    UserInfoMapper userInfoMapper;
    @RequestMapping(value = "/login.do")
    @ResponseBody
    public ServerResponse login(){
        UserInfo userInfo=userInfoMapper.selectByPrimaryKey(21);
       return  ServerResponse.createBySuccess("success",userInfo);

    }


}
