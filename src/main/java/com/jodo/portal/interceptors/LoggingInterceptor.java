//package com.jodo.portal.interceptors;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//@Component
//public class LoggingInterceptor implements HandlerInterceptor {
//    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // Wrap the request to cache the body
//        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
//
//        // Log request details
//        logger.info("Request URL: {}", wrappedRequest.getRequestURL());
//        logger.info("HTTP Method: {}", wrappedRequest.getMethod());
//        logger.info("Headers: {}", getHeadersAsString(wrappedRequest));
//        
//        StringBuilder requestBody = new StringBuilder();
//        BufferedReader reader = wrappedRequest.getReader();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            requestBody.append(line);
//        }
//        logger.info("Request Body: {}", requestBody.toString());
//
//        // Replace the original request with the wrapped one
//        request.setAttribute("cachedRequest", wrappedRequest);
//
//        return true; // Continue to the next interceptor or the handler
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        // Log response details
//        logger.info("Response Status: {}", response.getStatus());
//    }
//
//    private String getHeadersAsString(HttpServletRequest request) {
//        StringBuilder headers = new StringBuilder();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
//            httpHeaders.add(headerName, request.getHeader(headerName));
//        });
//        httpHeaders.forEach((key, value) -> headers.append(key).append(": ").append(value).append("\n"));
//        return headers.toString();
//    }
//}
