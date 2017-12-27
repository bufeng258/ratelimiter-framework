package com.bufeng.ratelimiter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.bufeng.ratelimiter.test.LoginService;

/**
 * @author liuhailong 2017/10/11
 */
public class LoginServiceTest extends TestBase {

    @Autowired
    private LoginService loginService;

    /**
     * 计数器算法限流测试
     */
    @Test
    public void addUserTest() throws InterruptedException {
        List<Runnable> tasks = Lists.newArrayList();
        int size = 11;
        for (int i = 0; i < size; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    loginService.addUser(1);
                }
            };
            tasks.add(runnable);
        }
        //模拟并发访问接口
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        int sleepTime = 1000 / size;
        long startTime = System.currentTimeMillis();
        while (startTime % 1000 != 0) {
            TimeUnit.MILLISECONDS.sleep(1);
            startTime = System.currentTimeMillis();
        }
        try {
            for (Runnable task: tasks) {
                executorService.submit(task);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
        } finally {
            executorService.shutdown();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("request consume time:" + (endTime - startTime));
        while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("Executors is not terminal");
        }
    }

    /**
     * Guava RateLimiter限流测试
     */
    @Test
    public void getUserTest() throws InterruptedException {
        List<Runnable> tasks = Lists.newArrayList();
        int size = 8;
        for (int i = 0; i < size; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    loginService.getUser(1);
                }
            };
            tasks.add(runnable);
        }
        //模拟并发访问接口
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        int sleepTime = 1000 / size;
        long startTime = System.currentTimeMillis();
        try {
            for (Runnable task: tasks) {
                executorService.submit(task);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
        } finally {
            executorService.shutdown();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("request consume time:" + (endTime - startTime));
        while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("Executors is not terminal");
        }
    }

}
