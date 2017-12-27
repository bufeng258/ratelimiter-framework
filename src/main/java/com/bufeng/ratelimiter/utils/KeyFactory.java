package com.bufeng.ratelimiter.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Key生成工厂类
 * 
 * @author liuhailong 2017/10/9
 */
public class KeyFactory {

    /**
     * 根据Annotation注解的需要限流的方法,生成RateLimiter关联的key
     * 
     * @param jp
     * @return
     */
    public static String createKey(JoinPoint jp) {
        StringBuilder sb = new StringBuilder();
        appendType(sb, getType(jp));
        Signature signature = jp.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature ms = (MethodSignature) signature;
            sb.append("#");
            sb.append(ms.getMethod().getName());
            sb.append("(");
            appendTypes(sb, ms.getMethod().getParameterTypes());
            sb.append(")");
        }
        return sb.toString();
    }

    private static Class<?> getType(JoinPoint jp) {
        if (jp.getSourceLocation() != null) {
            return jp.getSourceLocation().getWithinType();
        } else {
            return jp.getSignature().getDeclaringType();
        }
    }

    private static void appendTypes(StringBuilder sb, Class<?>[] types) {
        for (int size = types.length, i = 0; i < size; i++) {
            appendType(sb, types[i]);
            if (i < size - 1) {
                sb.append(",");
            }
        }
    }

    private static void appendType(StringBuilder sb, Class<?> type) {
        if (type.isArray()) {
            appendType(sb, type.getComponentType());
            sb.append("[]");
        } else {
            sb.append(type.getName());
        }
    }

}
