package com.bufeng.ratelimiter.strategy.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.bufeng.ratelimiter.aop.RateLimiterMethod;
import com.bufeng.ratelimiter.strategy.RateLimiterStrategy;

/**
 * 计数器算法限流实现
 *
 * @author liuhailong 2017/11/11
 */
@Service("counterRateLimiterStrategy")
public class CounterRateLimiterStrategy extends RateLimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CounterRateLimiterStrategy.class);

    /**
     * Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
     */
    private ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>> counters
        = new ConcurrentHashMap<>();

    @Override
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createRateLimiterKey(pjp, rateLimiterMethod);
        logger.info("counterRateLimiter handle start,key:{}", key);
        LoadingCache<Long, AtomicLong> counter = createCouter(key, rateLimiterMethod);
        //获取当前时间戳,然后取秒数来作为key进行计数统计和限流
        long currentSecond = System.currentTimeMillis() / 1000;
        long qps = rateLimiterMethod.qps();
        AtomicLong atomicLong = counter.get(currentSecond);
        if (atomicLong == null) {
            logger.info("counter is null,method:{}", pjp.getSignature().toLongString());
            return pjp.proceed();
        }
        if (atomicLong.incrementAndGet() <= qps) {
            return pjp.proceed();
        }
        //被限流后,进入限流处理逻辑
        return fallBackMethodExecute(key, pjp, rateLimiterMethod);
    }

    /**
     * 构造计数器,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     *
     * @param key               相同key返回同一个计数器
     * @param rateLimiterMethod
     * @return
     */
    private LoadingCache<Long, AtomicLong> createCouter(String key, RateLimiterMethod rateLimiterMethod) {
        LoadingCache<Long, AtomicLong> result = counters.get(key);
        if (result == null) {
            //Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
            LoadingCache<Long, AtomicLong> value = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS)
                .build(new CacheLoader<Long, AtomicLong>() {
                    @Override
                    public AtomicLong load(Long seconds) throws Exception {
                        return new AtomicLong(0);
                    }
                });
            result = value;
            LoadingCache<Long, AtomicLong> putByOtherThread = counters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        return result;
    }
}
