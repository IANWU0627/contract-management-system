package com.contracthub.aspect;

import com.contracthub.annotation.RequirePermission;
import com.contracthub.dto.ApiResponse;
import com.contracthub.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class PermissionAspect {

    private static final List<String> ADMIN_PERMISSIONS = Arrays.asList(
        "contract:delete", "contract:restore", "version:restore"
    );

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String permission = requirePermission.value();
        String currentUserRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 管理员拥有所有权限
        if ("ADMIN".equals(currentUserRole)) {
            return joinPoint.proceed();
        }

        // 检查敏感操作权限
        if (ADMIN_PERMISSIONS.contains(permission) && !"ADMIN".equals(currentUserRole)) {
            return ApiResponse.error(403, "无权执行此操作，需要管理员权限", "error.permission.adminRequired");
        }

        // 检查版本恢复权限 - 只有合同创建者或管理员可以恢复
        if ("version:restore".equals(permission)) {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 4) {
                Long operatorId = (Long) args[2];
                if (!currentUserId.equals(operatorId)) {
                    return ApiResponse.error(403, "无权恢复此版本", "error.permission.versionRestoreDenied");
                }
            }
        }

        return joinPoint.proceed();
    }
}
