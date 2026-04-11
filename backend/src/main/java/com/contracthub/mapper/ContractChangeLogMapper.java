package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractChangeLogMapper extends BaseMapper<ContractChangeLog> {
    
    @Select("SELECT c.*, ct.contract_no, ct.title as contract_title " +
            "FROM contract_change_log c " +
            "LEFT JOIN contract ct ON c.contract_id = ct.id " +
            "WHERE c.contract_id = #{contractId} " +
            "ORDER BY c.created_at DESC")
    List<Map<String, Object>> selectByContractId(Long contractId);
    
    @Select("SELECT c.*, ct.contract_no, ct.title as contract_title " +
            "FROM contract_change_log c " +
            "LEFT JOIN contract ct ON c.contract_id = ct.id " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectRecent(int limit);
    
    @Select("SELECT c.*, ct.contract_no, ct.title as contract_title " +
            "FROM contract_change_log c " +
            "LEFT JOIN contract ct ON c.contract_id = ct.id " +
            "WHERE c.operator_id = #{operatorId} " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectByOperator(Long operatorId, int limit);
}
