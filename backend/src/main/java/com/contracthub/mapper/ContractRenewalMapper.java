package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractRenewal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractRenewalMapper extends BaseMapper<ContractRenewal> {
    
    @Select("SELECT cr.*, c.title, c.contract_no FROM contract_renewal cr " +
            "LEFT JOIN contract c ON cr.contract_id = c.id WHERE cr.contract_id = #{contractId} " +
            "ORDER BY cr.created_at DESC")
    List<Map<String, Object>> selectRenewalsByContractId(Long contractId);
    
    @Select("SELECT cr.*, c.title, c.contract_no FROM contract_renewal cr " +
            "LEFT JOIN contract c ON cr.contract_id = c.id " +
            "WHERE (#{status} IS NULL OR cr.status = #{status}) " +
            "ORDER BY cr.created_at DESC")
    List<Map<String, Object>> selectRenewalsByStatus(String status);
}
