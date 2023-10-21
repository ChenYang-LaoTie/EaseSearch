package com.search.docsearch.aop;

import com.search.docsearch.utils.LogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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


    @AfterReturning(value = "pointCut()", returning = "returnObject")
    public void afterReturning(JoinPoint joinPoint, Object returnObject) {
        LogUtil.returnOperate(joinPoint, response.getStatus(), "", request);
    }

}
