package com.da.wechat.service.impl;


import com.da.wechat.config.WeChatConfig;
import com.da.wechat.domain.User;
import com.da.wechat.service.UserService;
import com.da.wechat.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

/**
 *  @Description: 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private WeChatConfig weChatConfig;

    @Autowired
    private UserService userService;


    @Override
    public User saveWeChatUser(String code) {

        //1、通过openAppid和openAppsecret和微信返回的code，拼接URL
        //String accessTokenUrl = String.format(WeChatConfig.getOpenAccessTokenUrl(),weChatConfig.getOpenAppid(),weChatConfig.getOpenAppsecret(),code);
        String accessTokenUrl = String.format(WeChatConfig.OPEN_ACCESS_TOKEN_URL, weChatConfig.getOpenAppid().trim(), weChatConfig.getOpenAppsecret().trim(), code);

        //2、通过URL再去回调微信接口 (使用了httpclient和gson工具）
        Map<String, Object> baseMap = HttpUtils.doGet(accessTokenUrl);

        //3、回调成功后获取access_token和openid
        if (baseMap == null || baseMap.isEmpty()) {
            return null;
        }
        String accessToken = (String) baseMap.get("access_token");
        String openId = (String) baseMap.get("openid");

        //4、去数据库查看该用户之前是否已经扫码登陆过（openid是用户唯一标志）
        //User dbUser = userMapper.findByopenid(openId);
        //if(dbUser!=null) { //如果用户已经存在，直接返回
          //  return dbUser;
        //}

        //4、access_token和openid拼接URL（openid是用户唯一标志）  获取用户信息
        String userInfoUrl = String.format(WeChatConfig.OPEN_USER_INFO_URL, accessToken, openId);

        //5、通过URL再去调微信接口获取用户基本信息
        Map<String, Object> baseUserMap = HttpUtils.doGet(userInfoUrl);

        if (baseUserMap == null || baseUserMap.isEmpty()) {
            return null;
        }

        //6、获取用户姓名、性别、城市、头像等基本信息
        String nickname = (String) baseUserMap.get("nickname");
        Integer sex = (Integer) baseUserMap.get("sex");
        String province = (String) baseUserMap.get("province");
        String city = (String) baseUserMap.get("city");
        String country = (String) baseUserMap.get("country");
        String headimgurl = (String) baseUserMap.get("headimgurl");

        StringBuilder sb = new StringBuilder(country).append("||").append(province).append("||").append(city);
        String finalAddress = sb.toString();

        try {
            //解决用户名乱码
            nickname = new String(nickname.getBytes("ISO-8859-1"), "UTF-8");
            finalAddress = new String(finalAddress.getBytes("ISO-8859-1"), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //7、新用户存入数据库
        User user = new User();
        user.setName(nickname);
        user.setHeadImg(headimgurl);
        user.setCity(finalAddress);
        user.setOpenid(openId);
        user.setSex(sex);
        user.setCreateTime(new Date());
        //userService.save(String.valueOf(user));
        return user;
    }
}
