package com.bufeng.ratelimiter.utils;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AOP工具类
 * 
 * @author liuhailong 2017/10/9
 */
public class AOPUtils {

    /**
     * 获取指定方法名的Method对象
     * 
     * @param joinPoint
     * @param methodName
     * @return
     */
    public static Method getMethodFromTarget(JoinPoint joinPoint, String methodName) {
        Method method = null;
        if (joinPoint.getSignature() instanceof MethodSignature) {
            method = getDeclaredMethod(joinPoint.getTarget().getClass(), methodName, getParameterTypes(joinPoint));
        }
        return method;
    }

    private static Method getDeclaredMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = type.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                method = getDeclaredMethod(superclass, methodName, parameterTypes);
            }
        }
        return method;
    }

    private static Class[] getParameterTypes(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getParameterTypes();
    }

}
