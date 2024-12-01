package com.zzy.yurpc.registry;

import cn.hutool.json.JSONUtil;
import com.zzy.yurpc.config.RegistryConfig;
import com.zzy.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry{
    private Client client;
    private KV kvClient;

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    /**
     * 初始化注册中心
     * @param registryConfig 注册中心配置
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
        kvClient = client.getKVClient();
    }

    /**
     * 注册服务(服务端)
     * @param serviceMetaInfo 服务元信息
     * 设置过期时间
     * 设置要存储的键值对
     * 将键值对与租约关联起来，并设置过期时间
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 创建Lease
        Lease leaseClient = client.getLeaseClient();
        //创建一个三十秒的租约
        long leaseId = leaseClient.grant(100).get().getID();

        //设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        //将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
    }

    /**
     * 注销服务(服务端)
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    /**
     * 获取服务(客户端)
     * @param serviceKey 服务名
     * @return
     * 根据服务名称作为前缀，从ETCD获取服务下的节点列表：
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {
        //前缀搜索，结尾一定要加一个/
        String searchPrefix = ETCD_ROOT_PATH + serviceKey;

        try{
            //前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                    getOption
            ).get().getKvs();
            // 解析服务信息
            return keyValues.stream()
                    .map(keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("获取服务列表失败",e);
        }
    }

    /**
     * 服务销毁
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        //释放资源
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }

    }
}

