package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.QuickCodeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 快速代码行 Mapper
 */
@Mapper
public interface QuickCodeItemMapper extends BaseMapper<QuickCodeItem> {
    
    @Select("SELECT * FROM quick_code_item WHERE header_id = #{headerId} ORDER BY sort_order")
    List<QuickCodeItem> selectByHeaderId(Long headerId);
    
    @Select("SELECT * FROM quick_code_item WHERE header_id = #{headerId} AND enabled = true ORDER BY sort_order")
    List<QuickCodeItem> selectEnabledByHeaderId(Long headerId);
}
