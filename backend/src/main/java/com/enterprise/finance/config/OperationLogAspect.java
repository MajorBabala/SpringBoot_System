package com.enterprise.finance.config;

import com.enterprise.finance.entity.OperationLog;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("execution(* com.enterprise.finance.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable throwable = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            writeOperationLog(joinPoint, start, throwable);
        }
    }

    private void writeOperationLog(ProceedingJoinPoint joinPoint, long start, Throwable throwable) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return;
        }

        HttpServletRequest request = servletAttrs.getRequest();
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api")) {
            return;
        }

        OperationLog log = new OperationLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            log.setUserId(principal.getUserId());
            log.setUsername(principal.getUsername());
        }

        String operation = request.getMethod() + " " + uri;
        log.setOperation(operation);
        log.setMethod(joinPoint.getSignature().toShortString());
        log.setParams(buildParams(request, throwable));
        log.setIp(resolveIp(request));
        log.setDuration((int) Math.max(0, System.currentTimeMillis() - start));
        log.setCreateTime(LocalDateTime.now());

        try {
            operationLogService.saveLog(log);
        } catch (Exception ignored) {
            // Logging must not affect business requests.
        }
    }

    private String buildParams(HttpServletRequest request, Throwable throwable) {
        String query = request.getQueryString();
        StringBuilder builder = new StringBuilder();
        if (query != null && !query.isBlank()) {
            builder.append("query=").append(query);
        } else {
            builder.append("query=");
        }

        if (throwable != null) {
            String message = throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
            if (message.length() > 200) {
                message = message.substring(0, 200);
            }
            builder.append("; error=").append(message.replaceAll("[\\r\\n]+", " "));
        }
        return builder.toString();
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int idx = xff.indexOf(',');
            return (idx > 0 ? xff.substring(0, idx) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
