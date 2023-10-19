package com.search.docsearch.aop;

import com.search.docsearch.utils.LogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.UUID;

import static com.search.docsearch.utils.LogUtil.TRACE_ID;

@Aspect
@Component
public class ManagementOperationLogAOP {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Pointcut("execution(* com.search.docsearch.controller..*.*(..))")
    public void pointCut() {
    }

    @Pointcut("@annotation(com.search.docsearch.aop.LogAction)")
    public void logAction() {
    }

    @Before(value = "pointCut()")
    public void doBefore(JoinPoint joinPoint) {
        String traceId = UUID.randomUUID().toString();
        if (!StringUtils.hasText(MDC.get(TRACE_ID))) {
            MDC.put(TRACE_ID, traceId);
        }
        LogUtil.beginOperate(joinPoint, request);
    }

    @AfterReturning(value = "pointCut()", returning = "returnObject")
    public void afterReturning(JoinPoint joinPoint, Object returnObject) {
        LogUtil.returnOperate(joinPoint, response.getStatus(), "", request);
    }


    @Before(value = "logAction()")
    public void actionDoBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAction action = method.getAnnotation(LogAction.class);

        System.out.println(action.detail());
    }

}
