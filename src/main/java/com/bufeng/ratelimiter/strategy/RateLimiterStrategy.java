package com.bufeng.ratelimiter.strategy;

import java.lang.reflect.Method;

import com.bufeng.ratelimiter.aop.RateLimiterMethod;
import com.bufeng.ratelimiter.exception.RateLimiterException;
import com.bufeng.ratelimiter.utils.AOPUtils;
import com.bufeng.ratelimiter.utils.KeyFactory;
import com.bufeng.ratelimiter.utils.SpelUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务限流策略抽象类
 *
 * @author liuhailong 2017/11/11
 */
public abstract class RateLimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterStrategy.class);

    /**
     * 资源分割符
     */
    private static final String SEPARATOR = "-";

    /**
     * 限流处理入口
     *
     * @param pjp
     * @param rateLimiterMethod
     * @return
     */
    public abstract Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable;

    /**
     * 限流后的降级方法执行
     *
     * @param pjp
     * @param rateLimiterMethod
     * @return
     * @throws Exception
     */
    protected Object fallBackMethodExecute(String rateLimiterKey, ProceedingJoinPoint pjp,
        RateLimiterMethod rateLimiterMethod) throws Exception {
        if (StringUtils.isNotBlank(rateLimiterMethod.fallBackMethod())) {
            Object obj = pjp.getTarget();
            Method method = AOPUtils.getMethodFromTarget(pjp, rateLimiterMethod.fallBackMethod());
            if (method == null) {
                logger.warn("fallBack method not exist,class:{},method:{}", obj.getClass().getName(),
                    rateLimiterMethod.fallBackMethod());
                throw new RateLimiterException(rateLimiterKey, "fallBack method not exist");
            }
            Object result = method.invoke(obj, pjp.getArgs());
            logger.info("fallBack method executed,class:{},method:{}", obj.getClass().getName(),
                rateLimiterMethod.fallBackMethod());
            return result;
        } else { //没有配置降级处理方法,则抛出异常
            logger.warn("request has been discarded,method:{}", pjp.getSignature().toLongString());
            throw new RateLimiterException(rateLimiterKey, "hit rateLimiter rule");
        }
    }

    /**
     * 构造限流资源key
     *
     * @param pjp
     * @param rateLimiterMethod
     * @return
     */
    protected String createRateLimiterKey(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) {
        StringBuilder sb = new StringBuilder();
        //使用注解时指定了key
        if (StringUtils.isNotBlank(rateLimiterMethod.key())) {
            sb.append(rateLimiterMethod.key());
        }
        //使用注解时指定了spel表达式
        if (StringUtils.isNotBlank(rateLimiterMethod.spel())) {
            //如果不是spel表达式，返回异常
            if (!rateLimiterMethod.spel().contains("#") && !rateLimiterMethod.spel().contains("'")) {
                throw new IllegalArgumentException(
                    "param is not a Spring EL expression,spel:" + rateLimiterMethod.spel());
            }
            MethodSignature methodSignature = (MethodSignature)pjp.getSignature();
            Object[] args = pjp.getArgs();
            Object obj = SpelUtils.parse(rateLimiterMethod.spel(), methodSignature.getMethod(), args);
            if (StringUtils.isNotBlank(sb.toString())) {
                sb.append(SEPARATOR);
            }
            sb.append(obj.toString());
        }
        //如果通过key和spel表达式构造出了一个非空字符串,那么直接返回
        if (StringUtils.isNotBlank(sb.toString())) {
            return sb.toString();
        }
        //默认采用方法签名构造key
        return KeyFactory.createKey(pjp);
    }

}
