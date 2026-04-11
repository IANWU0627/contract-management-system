package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import java.util.List;
import java.util.Map;

@Mapper
public interface ContractTagMapper extends BaseMapper<ContractTag> {
    
    @Select("SELECT ct.id, ct.name, ct.color, ct.description, ct.creator_id, ct.is_public, ct.created_at, " +
            "(SELECT COUNT(*) FROM contract_tag_relation ctr WHERE ctr.tag_id = ct.id) as usage_count " +
            "FROM contract_tag ct ORDER BY ct.created_at DESC")
    List<Map<String, Object>> selectTagsWithUsageCount();
    
    @Select("SELECT ctr.tag_id FROM contract_tag_relation ctr WHERE ctr.contract_id = #{contractId}")
    List<Long> selectTagIdsByContractId(Long contractId);
    
    @Select("SELECT ct.id, ct.name, ct.color FROM contract_tag ct " +
            "INNER JOIN contract_tag_relation ctr ON ct.id = ctr.tag_id " +
            "WHERE ctr.contract_id = #{contractId}")
    List<Map<String, Object>> selectTagsByContractId(Long contractId);
    
    @Insert("INSERT INTO contract_tag_relation (contract_id, tag_id) VALUES (#{contractId}, #{tagId})")
    int insertTagRelation(Long contractId, Long tagId);
    
    @Delete("DELETE FROM contract_tag_relation WHERE contract_id = #{contractId}")
    int deleteTagRelationsByContractId(Long contractId);
    
    @Delete("DELETE FROM contract_tag_relation WHERE tag_id = #{tagId}")
    int deleteTagRelationsByTagId(Long tagId);
    
    @Delete("DELETE FROM contract_tag_relation WHERE contract_id = #{contractId} AND tag_id = #{tagId}")
    int deleteTagRelation(Long contractId, Long tagId);
    
    @Select("SELECT c.id, c.title, c.contract_no, c.type, c.status, c.end_date FROM contract_tag_relation ctr " +
            "LEFT JOIN contract c ON ctr.contract_id = c.id WHERE ctr.tag_id = #{tagId}")
    List<Map<String, Object>> selectContractsByTagId(Long tagId);
}
