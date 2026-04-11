package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.mapper.ContractFavoriteMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractFavorite;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("hasAuthority('FAVORITE_MANAGE')")
public class FavoriteController {

    private final ContractFavoriteMapper favoriteMapper;
    private final ContractMapper contractMapper;

    public FavoriteController(ContractFavoriteMapper favoriteMapper, ContractMapper contractMapper) {
        this.favoriteMapper = favoriteMapper;
        this.contractMapper = contractMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") Long userId) {
        
        Page<ContractFavorite> pageParam = new Page<>(page, pageSize);
        var result = favoriteMapper.selectFavoritesByUserId(pageParam, userId);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (ContractFavorite fav : result.getRecords()) {
            Contract contract = contractMapper.selectById(fav.getContractId());
            if (contract != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", contract.getId());
                map.put("contractNo", contract.getContractNo());
                map.put("title", contract.getTitle());
                map.put("type", contract.getType());
                map.put("status", contract.getStatus());
                map.put("amount", contract.getAmount());
                map.put("endDate", contract.getEndDate());
                map.put("favoritedAt", fav.getCreatedAt());
                list.add(map);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        
        return ApiResponse.success(data);
    }

    @PostMapping("/{contractId}")
    public ApiResponse<Void> addFavorite(@PathVariable Long contractId, @RequestParam(defaultValue = "1") Long userId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return ApiResponse.error("合同不存在");
        }
        
        boolean exists = favoriteMapper.isFavorited(contractId, userId);
        if (exists) {
            return ApiResponse.error("已经收藏过了");
        }
        
        ContractFavorite favorite = new ContractFavorite();
        favorite.setContractId(contractId);
        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
        
        contract.setStarred(true);
        contractMapper.updateById(contract);
        
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{contractId}")
    public ApiResponse<Void> removeFavorite(@PathVariable Long contractId, @RequestParam(defaultValue = "1") Long userId) {
        favoriteMapper.deleteFavorite(contractId, userId);
        
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.eq("starred", true);
        List<Contract> starredContracts = contractMapper.selectList(wrapper);
        
        boolean hasOtherFavorites = starredContracts.stream().anyMatch(c -> c.getId().equals(contractId));
        if (!hasOtherFavorites) {
            Contract contract = contractMapper.selectById(contractId);
            if (contract != null) {
                contract.setStarred(false);
                contractMapper.updateById(contract);
            }
        }
        
        return ApiResponse.success(null);
    }

    @GetMapping("/check/{contractId}")
    public ApiResponse<Map<String, Object>> checkFavorite(@PathVariable Long contractId, @RequestParam(defaultValue = "1") Long userId) {
        boolean isFavorited = favoriteMapper.isFavorited(contractId, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", isFavorited);
        
        return ApiResponse.success(result);
    }
}
