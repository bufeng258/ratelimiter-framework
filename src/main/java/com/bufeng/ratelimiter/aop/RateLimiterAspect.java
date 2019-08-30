package com.bufeng.ratelimiter.aop;

import com.bufeng.ratelimiter.strategy.RateLimiterStrategy;
import com.bufeng.ratelimiter.strategy.RateLimiterStrategyFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 限流处理切面类
 *
 * @author liuhailong 2017/10/10
 */
@Aspect
@Component
@Order(1)
public class RateLimiterAspect {

    @Around("@annotation(rateLimiterMethod)")
    public Object method(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        RateLimiterStrategy rateLimiterStrategy = RateLimiterStrategyFactory.getRateLimiterStrategy(
            rateLimiterMethod.type());
        if (rateLimiterStrategy == null) {
            throw new IllegalArgumentException("unsupported rateLimiterType:" + rateLimiterMethod.type());
        }
        return rateLimiterStrategy.handle(pjp, rateLimiterMethod);
    }

}
