package com.zzy.yurpc.config;

import com.zzy.yurpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "yu-rpc";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口
     */
    private int serverPort = 8081;

    /**
     * 是否使用模拟数据
     */
    private boolean mock = false;

    /**
     * 序列化方式
     */
    private String serializer = SerializerKeys.JSON;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
