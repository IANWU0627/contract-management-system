package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractComment;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface ContractCommentMapper extends BaseMapper<ContractComment> {
    
    @Select("SELECT * FROM contract_comment WHERE contract_id = #{contractId} ORDER BY created_at DESC")
    List<ContractComment> selectByContractId(Long contractId);
}