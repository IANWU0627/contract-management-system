package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ReminderRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReminderRuleMapper extends BaseMapper<ReminderRule> {
    
    @Select("SELECT * FROM reminder_rule WHERE IFNULL(is_enabled, enabled) = 1 ORDER BY created_at DESC")
    List<ReminderRule> selectEnabledRules();
    
    @Select("SELECT * FROM reminder_rule ORDER BY created_at DESC")
    List<ReminderRule> selectAllRules();
}
