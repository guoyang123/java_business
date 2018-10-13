package com.neuedu.service;

import com.neuedu.common.ServerResponse;

public interface IUserService {

   public  ServerResponse login(String username,String password);

}
