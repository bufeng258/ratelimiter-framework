package com.bufeng.ratelimiter.strategy.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bufeng.ratelimiter.utils.AOPUtils;
import com.bufeng.ratelimiter.utils.KeyFactory;
import com.google.common.util.concurrent.RateLimiter;
import com.bufeng.ratelimiter.aop.RateLimiterMethod;
import com.bufeng.ratelimiter.strategy.RateLimiterStrategy;

/**
 * Guava RateLimiter限流实现
 * 
 * @author liuhailong 2017/11/11
 */
@Service("guavaRateLimiterStrategy")
public class GuavaRateLimiterStrategy implements RateLimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GuavaRateLimiterStrategy.class);

    private ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<String, RateLimiter>();

    /**
     * 一秒有多少微秒
     */
    private final long MICROSECONDS_OF_ONE_SECOND = 1000 * 1000L;

    @Override
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createKey(pjp, rateLimiterMethod);
        RateLimiter rateLimiter = createLimiter(key, rateLimiterMethod);
        if (rateLimiter == null) {
            logger.info("rateLimiter is null,method:{}", pjp.getSignature().toLongString());
            return pjp.proceed();
        }
        long qps = rateLimiterMethod.qps();
        long timeout = MICROSECONDS_OF_ONE_SECOND / qps;
        //        logger.info("timeout is:{} microsecond", timeout);
        //拿到令牌则执行方法
        if (rateLimiter.tryAcquire(timeout, TimeUnit.MICROSECONDS)) {
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
     * 构造RateLimiter关联的key
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
     * 构造RateLimiter,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     *
     * @param key
     *            相同key返回同一个rateLimiter
     * @param rateLimiterMethod
     * @return
     */
    private RateLimiter createLimiter(String key, RateLimiterMethod rateLimiterMethod) {
        RateLimiter result = limiters.get(key);
        if (result == null) {
            RateLimiter value = RateLimiter.create(rateLimiterMethod.qps());
            result = value;
            RateLimiter putByOtherThread = limiters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        return result;
    }
}
