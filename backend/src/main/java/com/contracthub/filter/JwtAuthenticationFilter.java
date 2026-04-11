package com.contracthub.filter;

import com.contracthub.util.JwtUtils;
import com.contracthub.entity.Permission;
import com.contracthub.entity.Role;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.RolePermissionMapper;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, RoleMapper roleMapper,
                                   RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper) {
        this.jwtUtils = jwtUtils;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
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

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
}
