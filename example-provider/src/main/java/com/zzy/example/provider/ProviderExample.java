package com.zzy.example.provider;


import com.zzy.example.common.service.UserService;
import com.zzy.yurpc.RpcApplication;
import com.zzy.yurpc.config.RegistryConfig;
import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.model.ServiceMetaInfo;
import com.zzy.yurpc.registry.LocalRegistry;
import com.zzy.yurpc.registry.Registry;
import com.zzy.yurpc.registry.RegistryFactory;
import com.zzy.yurpc.server.HttpServer;
import com.zzy.yurpc.server.VertxHttpServer;
import com.zzy.yurpc.server.tcp.VertxTcpServer;

/**
 * 服务提供者示例
 *
 */
public class ProviderExample {

    public static void main(String[] args) {

        // RPC框架初始化
        RpcApplication.init("yml");

        //注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        //注册服务到注册中心
        //1. 获取RPC配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        //2. 从RPC配置获取注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        //3. 通过工厂获取注册中心实例
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        //4. 声明一个服务元数据，将服务信息封装到元数据中
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName); // 服务名称
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost()); // 服务地址
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort()); // 服务端口
        try{
            //5. 注册服务
            registry.register(serviceMetaInfo);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
        // 启动web服务
//        HttpServer httpServer = new VertxHttpServer();
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
