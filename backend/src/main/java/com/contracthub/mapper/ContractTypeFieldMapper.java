package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractTypeField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractTypeFieldMapper extends BaseMapper<ContractTypeField> {
    
    @Select("SELECT * FROM contract_type_field WHERE contract_type = #{contractType} ORDER BY field_order ASC")
    List<ContractTypeField> selectByContractType(String contractType);
    
    @Select("SELECT DISTINCT contract_type FROM contract_type_field")
    List<String> selectAllContractTypes();
    
    @Select("SELECT COUNT(*) FROM contract_type_field WHERE contract_type = #{contractType} AND field_key = #{fieldKey}")
    int countByTypeAndKey(String contractType, String fieldKey);
    
    @Select("SELECT contract_type, COUNT(*) as count FROM contract_type_field GROUP BY contract_type")
    @Results({
        @Result(column = "contract_type", property = "contractType"),
        @Result(column = "count", property = "count")
    })
    List<Map<String, Object>> selectCountsByType();
}
