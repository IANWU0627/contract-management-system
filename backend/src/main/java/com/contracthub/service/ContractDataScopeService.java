package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.entity.Contract;
import com.contracthub.entity.User;
import com.contracthub.enums.UserRole;
import com.contracthub.mapper.UserMapper;
import com.contracthub.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 合同数据范围：管理员与法务看全部；其他用户看本人创建 + 与本人同部门的用户创建的合同。
 * 用户未设置部门时，仅本人创建。
 */
@Service
public class ContractDataScopeService {

    private final UserMapper userMapper;

    public ContractDataScopeService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean canViewAllContracts() {
        String role = SecurityUtils.getCurrentUserRole();
        return UserRole.ADMIN.name().equals(role) || UserRole.LEGAL.name().equals(role);
    }

    public void applyContractVisibilityFilter(QueryWrapper<Contract> wrapper) {
        if (canViewAllContracts()) {
            return;
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            wrapper.eq("creator_id", -1L);
            return;
        }
        User me = userMapper.selectById(currentUserId);
        String dept = trimToEmpty(me != null ? me.getDepartment() : null);
        if (dept.isEmpty()) {
            wrapper.eq("creator_id", currentUserId);
            return;
        }
        wrapper.and(w -> w.eq("creator_id", currentUserId)
                .or()
                .apply("creator_id IN (SELECT id FROM `user` WHERE department = {0})", dept));
    }

    public boolean canCurrentUserViewContract(Contract contract) {
        if (contract == null) {
            return false;
        }
        if (canViewAllContracts()) {
            return true;
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        if (Objects.equals(contract.getCreatorId(), currentUserId)) {
            return true;
        }
        return isSameDepartmentAs(contract.getCreatorId(), currentUserId);
    }

    private boolean isSameDepartmentAs(Long creatorId, Long currentUserId) {
        if (creatorId == null) {
            return false;
        }
        User me = userMapper.selectById(currentUserId);
        User creator = userMapper.selectById(creatorId);
        String d1 = trimToEmpty(me != null ? me.getDepartment() : null);
        String d2 = trimToEmpty(creator != null ? creator.getDepartment() : null);
        return !d1.isEmpty() && d1.equals(d2);
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
