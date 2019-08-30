package com.bufeng.ratelimiter.test;

/**
 * @author liuhailong
 * @date 2019/8/30
 */
public class QueryParam {

    private long userId;

    private String type;

    public long getUserId() {
        return userId;
    }

    public QueryParam setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public String getType() {
        return type;
    }

    public QueryParam setType(String type) {
        this.type = type;
        return this;
    }
}
