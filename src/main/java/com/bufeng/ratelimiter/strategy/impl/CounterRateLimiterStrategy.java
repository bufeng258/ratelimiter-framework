package com.bufeng.ratelimiter.strategy.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bufeng.ratelimiter.utils.AOPUtils;
import com.bufeng.ratelimiter.utils.KeyFactory;
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
public class CounterRateLimiterStrategy implements RateLimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CounterRateLimiterStrategy.class);

    /**
     * Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
     */
    private ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>> counters = new ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>>();

    @Override
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createKey(pjp, rateLimiterMethod);
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
        //被限流了,如果设置了降级方法，则执行降级方法
        if (StringUtils.isNotBlank(rateLimiterMethod.fallBackMethod())) {
            Object obj = pjp.getTarget();
            Method method = AOPUtils.getMethodFromTarget(pjp, rateLimiterMethod.fallBackMethod());
            if (method != null) {
                Object result = method.invoke(obj, pjp.getArgs());
                logger.info("fallBack method executed,class:{},method:{}", obj.getClass().getName(),
                    rateLimiterMethod.fallBackMethod());
                return result;
            }
            logger.warn("fallBack method not exist,class:{},method:{}", obj.getClass().getName(),
                rateLimiterMethod.fallBackMethod());
        }
        logger.info("request has been discarded,method:{}", pjp.getSignature().toLongString());
        return null;
    }

    /**
     * 构造counter关联的key
     *
     * @param pjp
     * @param rateLimiterMethod
     * @return
     */
    private String createKey(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) {
        //使用注解时指定了key
        if (StringUtils.isNotBlank(rateLimiterMethod.key())) {
            return rateLimiterMethod.key();
        }
        return KeyFactory.createKey(pjp);
    }

    /**
     * 构造计数器,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     *
     * @param key
     *            相同key返回同一个计数器
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
