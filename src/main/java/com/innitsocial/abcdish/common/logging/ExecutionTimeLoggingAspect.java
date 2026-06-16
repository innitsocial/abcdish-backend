package com.innitsocial.abcdish.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    @Value("${app.logging.service-duration-threshold-ms:250}")
    private long serviceDurationThresholdMs;

    @Around("within(com.innitsocial.abcdish..service..*)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration >= serviceDurationThresholdMs) {
                log.info(
                        "service={} method={} durationMs={}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        duration
                );
            }
        }
    }
}
