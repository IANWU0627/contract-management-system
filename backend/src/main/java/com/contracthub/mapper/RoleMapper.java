package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT * FROM role WHERE status = 1")
    List<Role> selectActiveRoles();
    
    @Select("SELECT * FROM role WHERE code = #{code}")
    Role selectByCode(String code);
}
