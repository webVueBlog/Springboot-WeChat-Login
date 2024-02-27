package com.da.wechat.service;


import com.da.wechat.domain.User;

/**
 *用户业务接口类
 */
public interface UserService {


     User saveWeChatUser(String code);

}
