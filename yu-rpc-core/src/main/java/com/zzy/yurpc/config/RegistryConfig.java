package com.zzy.yurpc.config;

import lombok.Data;

/**
 * PRC框架注册中心配置
 */
@Data
public class RegistryConfig {
    /**
     * 注册中心类别
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2379";

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     * 是指连接到etcd的时间
     */
    private Long timeout = 10000L;

}
