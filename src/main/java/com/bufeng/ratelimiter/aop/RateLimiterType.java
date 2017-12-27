package com.bufeng.ratelimiter.aop;

/**
 * 限流算法的类型
 * 
 * @author liuhailong 2017/11/11
 */
public enum RateLimiterType {
    GUAVA_RATELIMITER, //google guava提供的RateLimiter实现
    COUNTER_RATELIMITER; //计数器算法限流实现
}
