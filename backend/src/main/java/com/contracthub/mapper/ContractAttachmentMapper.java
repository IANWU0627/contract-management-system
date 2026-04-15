package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ContractAttachmentMapper extends BaseMapper<ContractAttachment> {
    
    @Select("SELECT id, contract_id, file_name, file_path AS file_url, file_size, file_type, description, file_category, created_at AS create_time FROM contract_attachment WHERE contract_id = #{contractId}")
    List<ContractAttachment> selectByContractId(Long contractId);
}
