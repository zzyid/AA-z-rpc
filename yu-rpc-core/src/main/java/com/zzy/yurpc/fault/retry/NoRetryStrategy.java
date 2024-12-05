package com.zzy.yurpc.fault.retry;

import com.zzy.yurpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 不重试，即只执行一次
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy{
    /**
     * 重试
     * @param callable 回调
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return  callable.call();
    }
}
