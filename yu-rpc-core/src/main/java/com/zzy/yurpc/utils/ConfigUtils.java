package com.zzy.yurpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 配置工具类
 */
public class ConfigUtils {
    /**
     * 加载配置对象
     * @param tClass 加载配置到哪个类
     * @param prefix 配置的前缀
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象,支持区分环境
     * @param tClass 加载配置到哪个类
     * @param prefix 配置的前缀
     * @param environment 选择环境
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)){
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        // 加载配置文件
        Props props = new Props(configFileBuilder.toString());
        return props.toBean(tClass, prefix);
    }

    /**
     * 加载配置类支持环境选择和文件类型选择
     * @param tClass
     * @param prefix
     * @param environment
     * @param fileType
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment, String fileType) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)){
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".").append(fileType);
        if("properties".equalsIgnoreCase(fileType)){
            // 加载配置文件
            Props props = new Props(configFileBuilder.toString());
            return props.toBean(tClass, prefix);
        }else if("yml".equalsIgnoreCase(fileType) || "yaml".equalsIgnoreCase(fileType)){
            // 加载 yml/yaml 文件
            // 1.获取资源输入流
            try (InputStream inputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(configFileBuilder.toString())) {
                //2. 创建Yaml对象
                Yaml yaml = new Yaml();
                //3. 解析YAML文件，即将YAML文本转换为Map对象
                Map<String, Object> data = yaml.load(inputStream);
                //4. 将Map转换为指定类型的对象
                return toBean(data, tClass, prefix);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load YAML file: " + configFileBuilder.toString(), e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    /**
     * 将 Map 转换为指定类型的对象
     * @param data Map 数据
     * @param tClass 目标类
     * @param prefix 配置的前缀
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    private static <T> T toBean(Map<String, Object> data, Class<T> tClass, String prefix) {
        // 这里可以使用反射或其他库（如 ModelMapper）来实现 Map 到对象的转换
        // 示例代码使用简单的手动转换
        try {
            //1.使用反射创建目标的对象实例
            T obj = tClass.getDeclaredConstructor().newInstance();
            //2. 遍历Map，根据前缀和字段名设置字段值
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                //3.如果指定了前缀，检查当前key是否以指定的前缀开头，不是则跳过
                if (prefix != null && !key.startsWith(prefix)) {
                    continue;
                }
                //这里是一个双重map，因此需要处理嵌套的情况
                Map<String, Object> value = (Map<String, Object>) entry.getValue();

                //5. 设置字段值
                setField(obj, value);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Map to bean", e);
        }
    }

    /**
     * 设置字段值
     * @param obj 对象
     * @param value 值
     * @throws IllegalAccessException 异常
     */
    private static void setField(Object obj, Map<String, Object> value) {
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            try {
                // 获取对象的字段
                Field field = obj.getClass().getDeclaredField(fieldName);
                // 设置字段可访问
                field.setAccessible(true);
                // 设置字段值
                field.set(obj, fieldValue);
            }catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
