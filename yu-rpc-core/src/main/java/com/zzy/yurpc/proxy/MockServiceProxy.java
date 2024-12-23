package com.zzy.yurpc.proxy;

import ch.qos.logback.core.util.InvocationGate;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Mock服务代理（JDK动态代理）
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    private final Faker faker = new Faker();

    /**
     * 调用代理
     * proxy: 被代理对象
     * method: 被调用的方法
     * args: 方法参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法的返回值类型，生成特定的默认值对象
        // method.getReturnType():返回方法的返回值类型
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke {}",method.getName());
        return getDefaultObject(returnType);
    }

    /**
     * 根据获取的返回值类型生成默认值对象
     */
    private Object getDefaultObject(Class<?> type){
        //基本类型
        // Type.isPrimitive()：判断是否是基本类型
        if(type.isPrimitive()){
            if(type == int.class){
                return 0;
            } else if(type == long.class){
                return 0L;
            } else if(type == float.class){
                return 0.0f;
            } else if (type == double.class) {
                return 0.0d;
            } else if (type == boolean.class) {
                return false;
            } else if (type == char.class) {
                return '\u0000';
            } else if (type == short.class) {
                return (short) 0;

            }
        }else {
            if(type == String.class){
                return faker.name().fullName();
            }else if (type == Date.class) {
                return faker.date().past(365, java.util.concurrent.TimeUnit.DAYS);
            } else if (type == Integer.class) {
                return 0;
            } else if (type == Long.class) {
                return 0L;
            } else if (type == Float.class) {
                return 0.0f;
            } else if (type == Double.class) {
                return 0.0d;
            } else if (type == Boolean.class) {
                return false;
            } else if (type == Character.class) {
                return '\u0000';
            } else if (type == Short.class) {
                return (short) 0;
            } else {
                // 其他引用类型返回 null
                return null;
            }

        }
        //如果返回的是引用类型，则返回null
        return null;
    }

}
