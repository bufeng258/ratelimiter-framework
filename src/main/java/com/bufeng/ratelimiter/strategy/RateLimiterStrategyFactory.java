package com.bufeng.ratelimiter.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bufeng.ratelimiter.aop.RateLimiterType;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 限流策略工厂类
 *
 * @author liuhailong
 * @date 2019/8/30
 */
public class RateLimiterStrategyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterStrategyFactory.class);

    /***
     * 限流策略实例map
     */
    private static final ConcurrentMap<RateLimiterType, RateLimiterStrategy> rateLimiterStrategyMap
        = new ConcurrentHashMap<>();

    /**
     * 注册限流策略实例
     *
     * @param rateLimiterType
     * @param rateLimiterStrategy
     */
    public static void register(RateLimiterType rateLimiterType, RateLimiterStrategy rateLimiterStrategy) {
        Preconditions.checkNotNull(rateLimiterType, "rateLimiterType can not be null");
        Preconditions.checkNotNull(rateLimiterStrategy, "rateLimiterStrategy can not be null");
        RateLimiterStrategy existStrategy = rateLimiterStrategyMap.put(rateLimiterType, rateLimiterStrategy);
        if (existStrategy == null) {
            LOGGER.info("register rateLimiterType:{} with rateLimiterStrategy:{} success", rateLimiterType.name(),
                rateLimiterStrategy.getClass().getCanonicalName());
            return;
        }
        if (existStrategy.equals(rateLimiterStrategy)) {
            LOGGER.info("register rateLimiterStrategy:{} replace rateLimiterStrategy:{}",
                rateLimiterStrategy.getClass().getCanonicalName(), existStrategy.getClass().getCanonicalName());
        }
    }

    /**
     * 获取限流策略实例
     *
     * @param rateLimiterType
     * @return
     */
    public static RateLimiterStrategy getRateLimiterStrategy(RateLimiterType rateLimiterType) {
        Preconditions.checkNotNull(rateLimiterType, "rateLimiterType can not be null");
        RateLimiterStrategy rateLimiterStrategy = rateLimiterStrategyMap.get(rateLimiterType);
        LOGGER.info("getRateLimiterStrategy rateLimiterType:{},result is:{}", rateLimiterType.name(),
            rateLimiterStrategy == null ? "null" : rateLimiterStrategy.getClass().getCanonicalName());
        return rateLimiterStrategy;
    }

}
