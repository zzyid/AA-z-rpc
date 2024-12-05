package com.zzy.yurpc.fault.tolerant;

import com.zzy.yurpc.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败 - 容错策略 (立刻通知外层调用方)
 * 遇到异常后，将异常再次抛出，交给外层处理
 */
public class FailFastTolerantStrategy implements TolerantStrategy{

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务报错", e);
    }
}
