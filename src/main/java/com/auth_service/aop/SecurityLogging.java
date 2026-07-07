package com.auth_service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SecurityLogging {

    @Pointcut("execution(public * com.auth_service.security.SecurityConfig.*(..))")
    public void securityConfigBeans() {
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
