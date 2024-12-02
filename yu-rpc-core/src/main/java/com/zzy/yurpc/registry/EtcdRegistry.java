package com.zzy.yurpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 使用etcd实现注册中心
 */
@Slf4j
public class EtcdRegistry implements Registry{
    private Client client;
    private KV kvClient;

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();


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
        // 初始化注册中心时，开启心跳检测
        heartBeat();
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
        long leaseId = leaseClient.grant(30).get().getID();

        //设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        //将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 注销服务(服务端)
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(serviceMetaInfo.getServiceNodeKey());
    }

    /**
     * 获取服务(客户端)
     * @param serviceKey 服务名
     * @return
     * 根据服务名称作为前缀，从ETCD获取服务下的节点列表：
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {

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
        // 下线节点
        // 遍历本节点所有的key
        for (String key : localRegisterNodeKeySet){
            try{
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        //释放资源
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }

    }

    /**
     * 心跳检测（服务端）
     * 使用CronUtil实现定时任务，对有所集合中的节点执行重新注册操作
     */
    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的key
                for (String key : localRegisterNodeKeySet){
                    try{
                        // 获取指定键的键值
                        // KeyValue：键值对
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get().getKvs();
                        // 该节点已过期（需要重启节点才能重新注册）
                        if(CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        // 节点未过期，重新注册（相当于续约）
                        KeyValue keyValue = keyValues.get(0);
                        // 获取键的值，并转换为String
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        // 反序列化
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        // 把服务信息注册到etcd
                        register(serviceMetaInfo);
                    }catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(key + "续约失败",e);
                    }
                }
            }
        });
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start(); // 启动
    }
}

