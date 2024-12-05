package com.zzy.yurpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zzy.yurpc.RpcApplication;
import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.constanl.RpcConstant;
import com.zzy.yurpc.fault.retry.RetryStrategy;
import com.zzy.yurpc.fault.retry.RetryStrategyFactory;
import com.zzy.yurpc.fault.tolerant.TolerantStrategy;
import com.zzy.yurpc.fault.tolerant.TolerantStrategyFactory;
import com.zzy.yurpc.loadbalancer.LoadBalancer;
import com.zzy.yurpc.loadbalancer.LoadBalancerFactory;
import com.zzy.yurpc.model.RpcRequest;
import com.zzy.yurpc.model.RpcResponse;
import com.zzy.yurpc.model.ServiceMetaInfo;
import com.zzy.yurpc.registry.Registry;
import com.zzy.yurpc.registry.RegistryFactory;
import com.zzy.yurpc.serializer.Serializer;
import com.zzy.yurpc.serializer.SerializerFactory;
import com.zzy.yurpc.server.tcp.VertxTcpClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

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

            //负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            // 将调用方法名(请求路径)作为负载均衡参数
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName",rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo  = loadBalancer.select(requestParams, serviceMetaInfosList);

            // 发送请求
            // 发送 TCP 请求

            RpcResponse rpcResponse;
            try {
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() ->
                        VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
                );
            } catch (Exception e){
                // 容错机制
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(null, e);
            }
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
