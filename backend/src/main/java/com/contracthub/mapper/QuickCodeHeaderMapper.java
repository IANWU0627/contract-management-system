package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.QuickCodeHeader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 快速代码头 Mapper
 */
@Mapper
public interface QuickCodeHeaderMapper extends BaseMapper<QuickCodeHeader> {
    
    @Select("SELECT * FROM quick_code_header WHERE code = #{code} AND ROWNUM = 1")
    QuickCodeHeader selectByCode(String code);
}
