package com.moongeul.backend.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class TimerAspect {

    @Around("@annotation(com.moongeul.backend.common.annotation.Timer)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            log.info("메서드 실행 시간 [{}]: {}ms",
                    joinPoint.getSignature().getName(),
                    stopWatch.getTotalTimeMillis());
        }
    }
}