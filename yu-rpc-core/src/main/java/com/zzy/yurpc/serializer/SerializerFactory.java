package com.zzy.yurpc.serializer;

import com.zzy.yurpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义序列化器工厂
 */
public class SerializerFactory {

//    /**
//     * 序列化器映射 。用于实现单例
//     */
//    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>(){{
//        put(SerializerKeys.JSON, new JsonSerializer());
//        put(SerializerKeys.HESSIAN, new HessianSerializer());
//        put(SerializerKeys.KRYO, new KryoSerializer());
//        put(SerializerKeys.JDK, new JdkSerializer());
//    }};

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        // getOrDefault() 方法会返回 KEY_SERIALIZER_MAP 中对应的 value，如果 KEY_SERIALIZER_MAP 中没有对应的 value，则返回第二个参数指定的 value。
//        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
        // 使用 SPI 加载
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
