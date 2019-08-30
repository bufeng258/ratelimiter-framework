# ratelimiter-framework
用于接口层的服务限流框架

# 项目简介
1. 在google guava的RateLimiter基础上，结合spring aop，实现的一个可用于接口限流的框架，项目需要依赖spring框架。<br>
2. 目前该限流框架支持：令牌桶和计数器限流算法。<br>
3. 支持限流降级方法配置，降级方法需要和被限流方法的参数列表保持一致。<br>
4. 支持spring el表达式动态获取方法参数值，用于更灵活的配置限流key，[spel表达式文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions)。

# 使用介绍
1. 该框架使用起来非常简单，通过项目中提供的打包脚本，将核心代码打到jar包中，并在项目中引入依赖即可使用。<br>
2. 目前只提供注解使用方式，低侵入性，只需要接口上加上@RateLimiterMethod注解，并填上符合自己要求的参数即可。<br>
3. 一个限流的接口例子如下：<br>
```Java
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
}
```
4. 一个使用spring el表达式构造限流key的例子如下，运用spel表达式后会将key中内容和spel表达式的内容拼接起来作为真正的限流资源key <br>
```Java
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @RateLimiterMethod(key = "loginService.queryUser", spel = "#queryParam.type", type = RateLimiterType.GUAVA_RATELIMITER, qps = 2)
    @Override
    public void queryUser(QueryParam queryParam) {
        logger.info("query user execute");
    }
}
```
