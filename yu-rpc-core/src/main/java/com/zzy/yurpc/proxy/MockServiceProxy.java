package com.zzy.yurpc.proxy;

import ch.qos.logback.core.util.InvocationGate;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock服务代理（JDK动态代理）
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {

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
     * @param Type
     * @return
     */
    private Object getDefaultObject(Class<?> Type){
        //基本类型
        // Type.isPrimitive()：判断是否是基本类型
        if(Type.isPrimitive()){
            if(Type == int.class){
                return 0;
            } else if(Type == long.class){
                return 0L;
            } else if(Type == float.class){
                return 0.0f;
            } else if (Type == double.class) {
                return 0.0d;
            } else if (Type == boolean.class) {
                return false;
            } else if (Type == char.class) {
                return '\u0000';
            } else if (Type == short.class) {
                return (short) 0;

            }
        }
        //如果返回的是引用类型，则返回null
        return null;
    }

}
