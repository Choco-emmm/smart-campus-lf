package com.choco.smartlf.aspect;

import com.choco.smartlf.annotation.AiRateLimit;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class AiRateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 环绕通知：拦截所有打了 @AiRateLimit 注解的方法
     */
    @Around("@annotation(aiRateLimit)")
    public Object checkAiLimit(ProceedingJoinPoint pjp, AiRateLimit aiRateLimit) throws Throwable {

        // 1. 获取当前用户 ID
        // 🚨 注意：如果这个方法是在 CompletableFuture 异步线程里执行的，
        // UserContext 可能会因为 ThreadLocal 丢失而拿到 null。
        // 你可以通过 pjp.getArgs() 去拿方法里的传参，或者确保你的异步线程有传递 Context 的机制。
        Long userId=null;
        try{
            userId = UserContext.getUserId();
        }catch (Exception e){
        }

        if (userId == null) {
            //从参数中的第4个来获取
            userId = (Long) pjp.getArgs()[3];
        }
        if (userId == null) {
            log.warn("【AI限流】用户未登录，请先登录");
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }
        log.info("【AI限流】用户ID: {}", userId);
        // 2. 组装 Redis Key 和 获取最大限制次数
        String redisKey = Constant.RATE_LIMIT_KEY + userId;
        int maxLimit = aiRateLimit.maxCount();

        // 3. 执行 Redis 自增
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);

        if (currentCount != null) {
            // 如果是今天第一次调用，设置过期时间为 24 小时
            if (currentCount == 1L) {
                stringRedisTemplate.expire(redisKey, 24, TimeUnit.HOURS);
            }
            log.info("【AI限流】用户 {} 调用 AI 接口，当前次数: {}/{}", userId, currentCount, maxLimit);

            // 🌟 4. 如果超限，直接拉闸！抛出异常中断执行
            if (currentCount > maxLimit) {
                log.warn("【AI限流】用户 {} 触发 AI 限流拦截，当前次数: {}/{}", userId, currentCount, maxLimit);
                throw new BusinessException(ResultCodeEnum.AI_CALL_LIMIT);
            }
        }

        // 5. 校验通过，放行去执行真正的目标方法
        return pjp.proceed();
    }
}