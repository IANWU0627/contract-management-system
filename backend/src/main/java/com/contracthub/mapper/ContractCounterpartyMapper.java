package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractCounterparty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ContractCounterpartyMapper extends BaseMapper<ContractCounterparty> {
    
    @Select("SELECT * FROM contract_counterparty WHERE contract_id = #{contractId} ORDER BY sort_order")
    List<ContractCounterparty> selectByContractId(Long contractId);
}
