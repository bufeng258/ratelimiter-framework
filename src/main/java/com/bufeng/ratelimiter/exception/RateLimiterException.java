package com.bufeng.ratelimiter.exception;

/**
 * 限流异常类
 *
 * @author liuhailong
 * @date 2019/8/30
 */
public class RateLimiterException extends RuntimeException {

    private String rateLimiterKey;

    public String getRateLimiterKey() {
        return rateLimiterKey;
    }

    public RateLimiterException setRateLimiterKey(String rateLimiterKey) {
        this.rateLimiterKey = rateLimiterKey;
        return this;
    }

    public RateLimiterException(String rateLimiterKey, String message) {
        super(message);
        this.rateLimiterKey = rateLimiterKey;
    }

    public RateLimiterException(String rateLimiterKey) {
        this.rateLimiterKey = rateLimiterKey;
    }

    @Override
    public String getMessage() {
        return rateLimiterKey + "-" + super.getMessage();
    }
}
