package com.choco.smartlf.annotation;

import java.lang.annotation.*;

/**
 * AI 接口限流注解
 * 贴上此注解的方法，会自动接入 Redis 的 24 小时频次限制
 */
@Target(ElementType.METHOD) // 只能打在方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiRateLimit {
    
    /**
     * 默认每天最大调用次数
     */
    int maxCount() default 2;
}