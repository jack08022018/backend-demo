package com.jpa.retry_config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class RetryAspect {
    final RetryConfig retryConfig;

    @Around("@annotation(retryActivity)")
    public Object retryLogic(ProceedingJoinPoint joinPoint, RetryActivity retryActivity) throws Throwable {
        var retryTemplate = retryConfig.getRetryTemplate(retryActivity.retryFor(), retryActivity.maxAttempts());
        return retryTemplate.execute(context -> {
            context.setAttribute("api-name", retryActivity.name());
//            Object[] args = joinPoint.getArgs();
//            if (args.length > 0 && args[0] instanceof BookingGrpcRequest) {
//                BookingGrpcRequest dto = (BookingGrpcRequest) args[0];
//                context.setAttribute("lmid", dto.getLmid());
//            }
            return joinPoint.proceed();
        });
    }
}
