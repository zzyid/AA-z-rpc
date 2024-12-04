package com.zzy.yurpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zzy.yurpc.RpcApplication;
import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.constanl.RpcConstant;
import com.zzy.yurpc.model.RpcRequest;
import com.zzy.yurpc.model.RpcResponse;
import com.zzy.yurpc.model.ServiceMetaInfo;
import com.zzy.yurpc.protocol.ProtocolConstant;
import com.zzy.yurpc.protocol.ProtocolMessage;
import com.zzy.yurpc.protocol.ProtocolMessageDecoder;
import com.zzy.yurpc.protocol.ProtocolMessageEncoder;
import com.zzy.yurpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.zzy.yurpc.protocol.enums.ProtocolMessageTypeEnum;
import com.zzy.yurpc.registry.Registry;
import com.zzy.yurpc.registry.RegistryFactory;
import com.zzy.yurpc.serializer.Serializer;
import com.zzy.yurpc.serializer.SerializerFactory;
import com.zzy.yurpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理（JDK 动态代理）
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
            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 2. 获取注册中心实例
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName); // 服务名称
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION); // 服务版本
            // 3.获取服务提供者服务地址列表
            List<ServiceMetaInfo> serviceMetaInfosList = registry.serviceDiscover(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfosList)){
                throw new RuntimeException("暂无服务地址");
            }
            //TODO 暂时先取第一个
            ServiceMetaInfo selectedServiceMetaInfo  = serviceMetaInfosList.get(0);

            // 发送请求
            // 发送 TCP 请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
        } catch (Exception e) {
            throw new RuntimeException("调用失败");
        }

    }

    /**
     * 发送 HTTP 请求
     *
     * @param selectedServiceMetaInfo
     * @param bodyBytes
     * @return
     * @throws IOException
     */
    private static RpcResponse doHttpRequest(ServiceMetaInfo selectedServiceMetaInfo, byte[] bodyBytes) throws IOException {
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        // 发送 HTTP 请求
        try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                .body(bodyBytes)
                .execute()) {
            byte[] result = httpResponse.bodyBytes();
            // 反序列化
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse;
        }
    }
}
