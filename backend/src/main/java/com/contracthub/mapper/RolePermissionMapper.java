package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    @Select("SELECT permission_id FROM role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);
    
    @Delete("DELETE FROM role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(Long roleId);
    
    @Insert("INSERT INTO role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    void insertRolePermission(Long roleId, Long permissionId);
    
    @Select("SELECT r.* FROM role r INNER JOIN role_permission rp ON r.id = rp.role_id WHERE rp.permission_id = #{permissionId}")
    List<com.contracthub.entity.Role> selectRolesByPermissionId(Long permissionId);
}
