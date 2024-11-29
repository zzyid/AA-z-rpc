package com.zzy.example.consumer;

import com.zzy.example.common.model.User;
import com.zzy.example.common.service.UserService;
import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.proxy.ServiceProxyFactory;
import com.zzy.yurpc.utils.ConfigUtils;

/**
 * 1.简单的测试读取配置文件
 * 2.测试mock代理
 */
public class ConsumerExample {
    public static void main(String[] args) {
//        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc","","yml");
//        System.out.println(rpc.toString());
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class, "yml");
        User user = new User();
        user.setName("yupi");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        }
        else {
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);

    }
}
