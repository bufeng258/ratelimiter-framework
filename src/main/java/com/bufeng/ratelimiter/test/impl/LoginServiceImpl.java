package com.bufeng.ratelimiter.test.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bufeng.ratelimiter.test.LoginService;
import com.bufeng.ratelimiter.aop.RateLimiterMethod;
import com.bufeng.ratelimiter.aop.RateLimiterType;

/**
 * @author liuhailong 2017/10/11
 */
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @RateLimiterMethod(type = RateLimiterType.COUNTER_RATELIMITER, qps = 10, fallBackMethod = "addUserFallBack")
    @Override
    public void addUser(long userId) {
        logger.info("add user execute");
    }

    public void addUserFallBack(long userId) {
        logger.info("add user fallBack method execute");
    }

    @RateLimiterMethod(type = RateLimiterType.GUAVA_RATELIMITER, qps = 5, fallBackMethod = "getUserFallBack")
    @Override
    public void getUser(long userId) {
        logger.info("get user execute");
    }

    public void getUserFallBack(long userId) {
        logger.info("get user fallBack method execute");
    }
}
