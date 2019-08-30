package com.bufeng.ratelimiter.utils;

import java.lang.reflect.Method;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spel表达式解析工具
 *
 * @author liuhailong
 * @date 2019/8/30
 */
public class SpelUtils {
    /**
     * spel表达式解析
     *
     * @param spel   表达式
     * @param method 目标方法
     * @param args   方法参数
     * @return 解析返回后的对象
     */
    public static Object parse(String spel, Method method, Object[] args) {
        //获取被拦截方法参数名列表
        LocalVariableTableParameterNameDiscoverer localVariable =
            new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = localVariable.getParameterNames(method);
        //使用SPEL进行解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        Expression expression = parser.parseExpression(spel);
        return expression.getValue(context, Object.class);
    }
}
