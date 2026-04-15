package com.contracthub.filter;

import com.contracthub.util.JwtUtils;
import com.contracthub.service.SessionConfigService;
import com.contracthub.entity.Permission;
import com.contracthub.entity.Role;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.mapper.UserSessionMapper;
import com.contracthub.entity.UserSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Duration AUTHORITY_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration LAST_ACTIVE_UPDATE_INTERVAL = Duration.ofMinutes(2);

    private final JwtUtils jwtUtils;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserSessionMapper userSessionMapper;
    private final SessionConfigService sessionConfigService;
    private final Map<String, CachedAuthorities> authorityCache = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(JwtUtils jwtUtils, RoleMapper roleMapper,
                                   RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper,
                                   UserSessionMapper userSessionMapper,
                                   SessionConfigService sessionConfigService) {
        this.jwtUtils = jwtUtils;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.userSessionMapper = userSessionMapper;
        this.sessionConfigService = sessionConfigService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 放行公开接口
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtils.validateToken(token) && !jwtUtils.isTokenExpired(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                // 设置用户信息到请求属性
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                List<SimpleGrantedAuthority> authorities = resolveAuthorities(role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Keep session last-active timestamp fresh for session details and anomaly checks.
                LambdaQueryWrapper<UserSession> sessionWrapper = new LambdaQueryWrapper<>();
                sessionWrapper.eq(UserSession::getToken, token);
                UserSession session = userSessionMapper.selectOne(sessionWrapper);
                if (session == null || !userId.equals(session.getUserId())) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"会话不存在或已失效，请重新登录\"}");
                    return;
                }
                if (isSessionTimedOut(session)) {
                    userSessionMapper.deleteById(session.getId());
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"会话已超时，请重新登录\"}");
                    return;
                }
                maybeUpdateLastActive(session);
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/contracts/next-number") ||
               path.equals("/error") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }

    private boolean isSessionTimedOut(UserSession session) {
        int timeoutMinutes = sessionConfigService.getSessionTimeout(session.getUserId());
        if (timeoutMinutes <= 0) {
            return false;
        }
        LocalDateTime lastActive = session.getLastActiveTime() != null ? session.getLastActiveTime() : session.getLoginTime();
        if (lastActive == null) {
            return false;
        }
        return lastActive.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
    }

    private List<SimpleGrantedAuthority> resolveAuthorities(String role) {
        LocalDateTime now = LocalDateTime.now();
        CachedAuthorities cached = authorityCache.get(role);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return new ArrayList<>(cached.authorities());
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        Role roleEntity = roleMapper.selectByCode(role);
        if (roleEntity != null) {
            List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleEntity.getId());
            if (permissionIds != null && !permissionIds.isEmpty()) {
                List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
                for (Permission permission : permissions) {
                    if (permission != null && permission.getCode() != null && !permission.getCode().isBlank()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    }
                }
            }
        }

        authorityCache.put(role, new CachedAuthorities(new ArrayList<>(authorities), now.plus(AUTHORITY_CACHE_TTL)));
        return authorities;
    }

    private void maybeUpdateLastActive(UserSession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = session.getLastActiveTime() != null ? session.getLastActiveTime() : session.getLoginTime();
        if (lastActive != null && lastActive.plus(LAST_ACTIVE_UPDATE_INTERVAL).isAfter(now)) {
            return;
        }
        session.setLastActiveTime(now);
        userSessionMapper.updateById(session);
    }

    private record CachedAuthorities(List<SimpleGrantedAuthority> authorities, LocalDateTime expiresAt) {}
}
