package com.auth_service.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ControllerLogging {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerLayer(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.info("CONTROLLER: IN - URI: {}, Method: {}, User: {}",
                request.getRequestURI(),
                request.getMethod(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous");

        Object result = joinPoint.proceed();

        log.info("CONTROLLER: OUT - URI: {}, Status: 200/500", request.getRequestURI());
        return result;
    }
}
