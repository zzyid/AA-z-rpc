package com.zzy.yurpc.fault.retry;

import com.zzy.yurpc.spi.SpiLoader;

/**
 * 重试策略工厂(用于获取重试器对象)
 */
public class RetryStrategyFactory {
    static {
        SpiLoader.load(RetryStrategy.class);
    }
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key){
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
