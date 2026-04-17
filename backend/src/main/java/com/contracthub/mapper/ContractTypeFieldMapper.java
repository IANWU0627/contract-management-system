package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractTypeField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Update;
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

    @Update("""
            UPDATE contract_type_field
            SET contract_type = #{contractType},
                field_key = #{fieldKey},
                field_label = #{fieldLabel},
                field_label_en = #{fieldLabelEn},
                field_type = #{fieldType},
                required = #{required},
                show_in_list = #{showInList},
                show_in_form = #{showInForm},
                field_order = #{fieldOrder},
                placeholder = #{placeholder},
                placeholder_en = #{placeholderEn},
                default_value = #{defaultValue},
                options = #{options},
                min_value = #{minValue},
                max_value = #{maxValue},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    int updateByIdWithoutQuickCodeId(ContractTypeField field);
}
