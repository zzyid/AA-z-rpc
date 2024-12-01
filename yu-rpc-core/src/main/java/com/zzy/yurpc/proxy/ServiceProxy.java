package com.zzy.yurpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zzy.yurpc.RpcApplication;
import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.constanl.RpcConstant;
import com.zzy.yurpc.model.RpcRequest;
import com.zzy.yurpc.model.RpcResponse;
import com.zzy.yurpc.model.ServiceMetaInfo;
import com.zzy.yurpc.registry.Registry;
import com.zzy.yurpc.registry.RegistryFactory;
import com.zzy.yurpc.serializer.JdkSerializer;
import com.zzy.yurpc.serializer.Serializer;
import com.zzy.yurpc.serializer.SerializerFactory;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理（JDK 动态代理）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig("yml").getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 2. 获取注册中心实例
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName); // 服务名称
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION); // 服务版本
            // 3.获取服务提供者请求列表
            List<ServiceMetaInfo> serviceMetaInfosList = registry.serviceDiscover(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfosList)){
                throw new RuntimeException("暂无服务地址");
            }
            //TODO 暂时先取第一个
            ServiceMetaInfo selectedServiceMetaInfo  = serviceMetaInfosList.get(0);

            // 发送请求
            // todo 注意，这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
            // 4.根据服务提供者地址发送请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
