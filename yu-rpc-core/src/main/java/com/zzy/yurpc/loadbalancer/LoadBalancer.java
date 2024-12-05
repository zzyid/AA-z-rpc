package com.zzy.yurpc.loadbalancer;

import com.zzy.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器（消费端使用）
 * 这是通用接口
 */
public interface LoadBalancer {
    /**
     * 选择服务节点调用
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    ServiceMetaInfo select(Map<String,Object>requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
