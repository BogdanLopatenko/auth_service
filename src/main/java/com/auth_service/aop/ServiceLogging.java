package com.auth_service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLogging {

    @Pointcut("execution(public * com.auth_service.service..*.*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(public * com.auth_service.security.SecurityConfig.*(..))")
    public void securityConfigBeans() {
    }


    @Around("serviceLayer()")
    public Object logServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {

        String signature = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        log.info("AOP: method entrance: {}", signature);

        Throwable caughtException = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            caughtException = e;
            throw e;
        } finally {

            long duration = System.currentTimeMillis() - startTime;

            if (caughtException == null) {
                log.info("AOP: method exit (OK): {}. Executed in {} ms.", signature, duration);
            } else {
                log.warn("AOP: method exit (FAILED): {}. Executed in {} ms.", signature, duration);
            }
        }
    }

    @AfterReturning(pointcut = "securityConfigBeans()", returning = "result")
    public void logBeanCreation(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String returnType = result.getClass().getSimpleName();

        log.info("SECURITY_CONFIG: Successfully created bean {} (Type: {})",
                methodName,
                returnType);
    }

    @AfterThrowing(pointcut = "securityConfigBeans()", throwing = "ex")
    public void logConfigError(JoinPoint joinPoint, Throwable ex) {
        log.error("SECURITY_CONFIG: Failed to create bean {} due to error: {}",
                joinPoint.getSignature().getName(),
                ex.getMessage(),
                ex);
    }

}
