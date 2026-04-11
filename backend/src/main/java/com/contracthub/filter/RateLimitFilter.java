package com.contracthub.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 300;
    private static final long WINDOW_SIZE_MS = 60 * 1000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // 放行公开接口和静态资源
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        String clientId = getClientIdentifier(httpRequest);
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientId, k -> new RateLimitInfo());
        
        long currentTime = System.currentTimeMillis();
        
        synchronized (info) {
            // 重置窗口
            if (currentTime - info.windowStart > WINDOW_SIZE_MS) {
                info.count.set(0);
                info.windowStart = currentTime;
            }
            
            info.count.incrementAndGet();
            
            if (info.count.get() > MAX_REQUESTS_PER_MINUTE) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                return;
            }
        }
        
        // 添加速率限制头
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - info.count.get())));
        
        chain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // 优先使用X-Forwarded-For头（反向代理场景）
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        // 使用X-Real-IP头
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        // 使用远程地址
        return request.getRemoteAddr();
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/contracts/next-number") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/error") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".ico") ||
               path.endsWith(".woff") ||
               path.endsWith(".woff2");
    }
    
    private static class RateLimitInfo {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }
}
