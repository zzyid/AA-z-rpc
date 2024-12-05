package com.zzy.yurpc.fault.retry;

import com.github.rholder.retry.*;
import com.zzy.yurpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔的重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 指定当遇到异常就重试
                .retryIfExceptionOfType(Exception.class)
                // 指定重试等待策略，选择fixedWait固定时间间隔策略
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // 指定重试停止策略，stopAfterAttempt超过最大重试次数停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 重试工作
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
