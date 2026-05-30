package com.innitsocial.abcdish.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    @Around("within(com.innitsocial.abcdish..service..*)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;

            log.info(
                    "service={} method={} durationMs={}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    duration
            );
        }
    }
}