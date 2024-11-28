package com.zzy.example.provider;


import com.zzy.example.common.service.UserService;
import com.zzy.yurpc.registry.LocalRegistry;
import com.zzy.yurpc.server.HttpServer;
import com.zzy.yurpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8081);
    }
}