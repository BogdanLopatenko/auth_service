package com.auth_service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GlobalExceptionLogging {

    @Pointcut("execution(public * com.auth_service.controller..*.*(..))")
    public void controllerLayer() {
    }

    @Pointcut("execution(public * com.auth_service.service..*.*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(public * com.auth_service.security..*.*(..))")
    public void securityLayer() {
    }

    @AfterThrowing(pointcut = "controllerLayer() || serviceLayer() || securityLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();


        log.error("EXCEPTION: Method {}.{}() has finished with exception: {}",
                className,
                methodName,
                ex.getMessage(),
                ex);
    }

}
