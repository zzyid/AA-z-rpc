package com.zzy.example.common.service;

import com.zzy.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {
    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 获取数字,测试Mock代理服务是否生效
     * @return
     */
    default short getNumber(){
        return 1;
    }
}
