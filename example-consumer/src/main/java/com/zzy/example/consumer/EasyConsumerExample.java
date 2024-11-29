package com.zzy.example.consumer;


import com.zzy.example.common.model.User;
import com.zzy.example.common.service.UserService;
import com.zzy.yurpc.proxy.ServiceProxyFactory;


/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // todo 需要获取 UserService 的实现类对象
        // 动态代理
        // 获取代理,每次使用userService都会调用ServiceProxy的invoke方法
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("yupi");
        // 调用
        // 调用getUser方法之前，会调用ServiceProxy的invoke方法
        User newUser = userService.getUser(user);
        if (newUser != null) {

            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
