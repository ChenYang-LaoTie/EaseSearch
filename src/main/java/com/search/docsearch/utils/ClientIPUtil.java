package com.search.docsearch.utils;

import javax.servlet.http.HttpServletRequest;

public class ClientIPUtil {

    public static String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "X-Real-IP"};

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (isValidIp(ip)) {
                return extractIp(ip);
            }
        }

        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip);
    }

    private static String extractIp(String ip) {
        if (ip.contains(",")) {
            return ip.split(",")[0];
        }
        return ip;
    }

}
