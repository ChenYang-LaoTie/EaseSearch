package com.search.docsearch.utils;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    public static final  String TRACE_ID = "TRACE_ID";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogUtil.class);


    public static void returnOperate(JoinPoint joinPoint, int status, String message, HttpServletRequest request) {
        returnLog log = new returnLog();
        log.setTraceId(MDC.get(TRACE_ID));

        log.setType("Official website operation");

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setTime(dateTime.format(formatter));

        log.setFunc(String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName()));

        log.setRequestUrl(request.getRequestURI());
        log.setMethod(request.getMethod());

        log.setAppIP(ClientIPUtil.getClientIpAddress(request));

        log.setStatus(status);
        log.setMessage(message);

        String jsonLog = JSON.toJSONString(log);
        logger.info("operationLog:{}", jsonLog);
    }

    public static void beginOperate(JoinPoint joinPoint, HttpServletRequest request) {
        beginLog log = new beginLog();
        log.setTraceId(MDC.get(TRACE_ID));

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setTime(dateTime.format(formatter));

        log.setRequestUrl(request.getRequestURI());
        log.setMethod(request.getMethod());

        String jsonLog = JSON.toJSONString(log);
        logger.info("operationLog:{}", jsonLog);
    }

    public static void sourceOperate(String detail) {
        sourceLog log = new sourceLog();
        log.setTraceId(MDC.get(TRACE_ID));

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setTime(dateTime.format(formatter));

        log.setDetail(detail);

        String jsonLog = JSON.toJSONString(log);
        logger.info("operationLog:{}", jsonLog);
    }


    @Data
    public static class sourceLog implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String traceId;

        private String time;

        private String detail;
    }


    @Data
    public static class returnLog implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String traceId;

        private String type;

        private String time;

        private String func;

        private String eventDetails;

        private String requestUrl;

        private String method;

        private String appIP;

        private int status;

        private String message;

        private String ErrorLog;

    }

    @Data
    public static class beginLog implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String traceId;

        private String time;

        private String requestUrl;

        private String method;
    }

}
