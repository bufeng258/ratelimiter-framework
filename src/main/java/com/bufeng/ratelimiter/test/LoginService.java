package com.bufeng.ratelimiter.test;

import org.springframework.stereotype.Service;

/**
 * @author liuhailong 2017/10/11
 */
@Service
public interface LoginService {

    void addUser(long userId);

    void getUser(long userId);

}
