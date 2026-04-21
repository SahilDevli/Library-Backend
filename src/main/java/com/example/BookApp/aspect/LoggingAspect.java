package com.example.BookApp.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Intercepts every method in every controller.
     * Logs: HTTP method, URL, called method, args, execution time, result status.
     */
    @Around("execution(* com.example.BookApp.controller.*.*(..))")
    public Object logControllerCall(ProceedingJoinPoint jp) throws Throwable {

        String className  = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();
        Object[] args     = jp.getArgs();

        // Get HTTP request info (route + method)
        String httpMethod = "UNKNOWN";
        String url        = "UNKNOWN";
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            httpMethod = request.getMethod();
            url        = request.getRequestURI();
        }

        log.info("──────────────────────────────────────────");
        log.info("→ REQUEST  : {} {}", httpMethod, url);
        log.info("→ HANDLER  : {}.{}()", className, methodName);
        log.info("→ ARGS     : {}", Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("← STATUS   : SUCCESS");
            log.info("← DURATION : {}ms", duration);
            log.info("──────────────────────────────────────────");
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("← STATUS   : FAILED — {}", e.getMessage());
            log.error("← DURATION : {}ms", duration);
            log.error("──────────────────────────────────────────");
            throw e;
        }
    }
}

