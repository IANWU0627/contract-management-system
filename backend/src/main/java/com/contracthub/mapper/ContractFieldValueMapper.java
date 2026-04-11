package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractFieldValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractFieldValueMapper extends BaseMapper<ContractFieldValue> {
    
    @Select("SELECT * FROM contract_field_value WHERE contract_id = #{contractId}")
    List<ContractFieldValue> selectByContractId(@Param("contractId") Long contractId);
    
    @Select("SELECT field_key, field_value FROM contract_field_value WHERE contract_id = #{contractId}")
    List<Map<String, Object>> selectKeyValueByContractId(@Param("contractId") Long contractId);
    
    @Insert("INSERT INTO contract_field_value (contract_id, field_key, field_value, created_at, updated_at) " +
            "VALUES (#{contractId}, #{fieldKey}, #{fieldValue}, NOW(), NOW())")
    int insertFieldValue(ContractFieldValue fieldValue);
    
    @Update("UPDATE contract_field_value SET field_value = #{fieldValue}, updated_at = NOW() " +
            "WHERE contract_id = #{contractId} AND field_key = #{fieldKey}")
    int updateFieldValue(@Param("contractId") Long contractId, @Param("fieldKey") String fieldKey, 
                        @Param("fieldValue") String fieldValue);
    
    @Delete("DELETE FROM contract_field_value WHERE contract_id = #{contractId}")
    int deleteByContractId(@Param("contractId") Long contractId);
    
    @Delete("DELETE FROM contract_field_value WHERE contract_id = #{contractId} AND field_key = #{fieldKey}")
    int deleteByContractIdAndFieldKey(@Param("contractId") Long contractId, @Param("fieldKey") String fieldKey);
}
