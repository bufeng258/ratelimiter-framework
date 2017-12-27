package com.bufeng.ratelimiter.strategy;

import org.aspectj.lang.ProceedingJoinPoint;

import com.bufeng.ratelimiter.aop.RateLimiterMethod;

/**
 * 服务限流策略接口
 * 
 * @author liuhailong 2017/11/11
 */
public interface RateLimiterStrategy {

    /**
     * 限流处理入口
     * 
     * @param pjp
     * @param rateLimiterMethod
     * @return
     */
    Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable;

}
