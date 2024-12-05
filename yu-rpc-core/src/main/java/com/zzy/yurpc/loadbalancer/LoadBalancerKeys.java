package com.zzy.yurpc.loadbalancer;

/**
 * 负载均衡器键名常量
 */
public interface LoadBalancerKeys {

    String ROUND_ROBIN = "roundRobin"; // 轮询

    String RANDOM = "random"; // 随机

    String CONSISTENT_HASH = "consistentHash"; // 一致性哈希
}
