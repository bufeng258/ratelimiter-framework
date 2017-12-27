package com.bufeng.ratelimiter.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.bufeng.ratelimiter.strategy.RateLimiterStrategy;

/**
 * 限流处理切面类
 * 
 * @author liuhailong 2017/10/10
 */
@Aspect
@Component
@Order(1)
public class RateLimiterAspect {

    @Autowired
    private RateLimiterStrategy guavaRateLimiterStrategy;

    @Autowired
    private RateLimiterStrategy counterRateLimiterStrategy;

    @Around("@annotation(rateLimiterMethod)")
    public Object method(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        Object result = null;
        switch (rateLimiterMethod.type()) {
            case GUAVA_RATELIMITER:
                result = guavaRateLimiterStrategy.handle(pjp, rateLimiterMethod);
                break;
            case COUNTER_RATELIMITER:
                result = counterRateLimiterStrategy.handle(pjp, rateLimiterMethod);
                break;
        }
        return result;
    }

}
