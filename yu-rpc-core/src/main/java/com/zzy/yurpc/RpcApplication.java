package com.zzy.yurpc;

import com.zzy.yurpc.config.RpcConfig;
import com.zzy.yurpc.constanl.RpcConstant;
import com.zzy.yurpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC 框架应用
 * 相当于holder，存放了项目全局用到的变量，双检锁单例模式
 */
@Slf4j
public class RpcApplication {
    // volatile: 确保在多线程中可见
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init,config = {}",newRpcConfig.toString());
    }


    /**
     * 初始化
     */
    public static void init(){
        RpcConfig newRpcConfig;
        try {
            // 尝试从配置文件中加载配置
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 配置加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 如果配置文件类型为 yaml 或 yml，使用此方法
     */
    public static void init(String fileType){
        RpcConfig newRpcConfig;
        try {
            // 尝试从配置文件中加载配置
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX,"",fileType);
        } catch (Exception e) {
            // 配置加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * 能确保在多线程环境下只会初始化rpcConfig对象一次
     */
    public static RpcConfig getRpcConfig() {
        // 双检锁单例模式
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }

    /**
     * 获取配置
     * 能确保在多线程环境下只会初始化rpcConfig对象一次
     * 如果配置文件类型为 yaml 或 yml，使用此方法
     * @param fileType
     * @return
     */
    public static RpcConfig getRpcConfig(String fileType) {
        // 双检锁单例模式
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init(fileType);
                }
            }
        }
        return rpcConfig;
    }
}
