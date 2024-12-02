package com.zzy.yurpc.registry;

import com.zzy.yurpc.config.RegistryConfig;
import com.zzy.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 注册中心接口
 * 使用SPI机制，后续可以使用多种注册中心
 */
public interface Registry {

    /**
     * 初始化
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务(服务端)
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException;

    /**
     * 注销服务(服务端)
     * @param serviceMetaInfo
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 获取服务(客户端)
     * @param serviceKey
     * @return
     */
    List<ServiceMetaInfo> serviceDiscover(String serviceKey);

    /**
     * 服务销毁
     */
    void destroy();

    /**
     * 心跳检测（服务端）
     */
    void heartBeat();

    /**
     * 监听（消费端）
     */
    void watch(String serviceNodeKey);
}
