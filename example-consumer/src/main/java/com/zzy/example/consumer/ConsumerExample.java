package com.zzy.example.consumer;

import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.utils.ConfigUtils;

/**
 * 简单的测试读取配置文件
 */
public class ConsumerExample {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc","","yml");
        System.out.println(rpc.toString());

    }
}
