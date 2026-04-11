package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface ContractFavoriteMapper extends BaseMapper<ContractFavorite> {
    
    @Select("SELECT cf.*, c.title, c.contract_no, c.type, c.status, c.end_date FROM contract_favorite cf " +
            "LEFT JOIN contract c ON cf.contract_id = c.id WHERE cf.user_id = #{userId} ORDER BY cf.created_at DESC")
    IPage<ContractFavorite> selectFavoritesByUserId(Page<?> page, Long userId);
    
    @Insert("INSERT INTO contract_favorite (contract_id, user_id) VALUES (#{contractId}, #{userId})")
    int insertFavorite(Long contractId, Long userId);
    
    @Delete("DELETE FROM contract_favorite WHERE contract_id = #{contractId} AND user_id = #{userId}")
    int deleteFavorite(Long contractId, Long userId);
    
    @Select("SELECT COUNT(*) > 0 FROM contract_favorite WHERE contract_id = #{contractId} AND user_id = #{userId}")
    boolean isFavorited(Long contractId, Long userId);
}
