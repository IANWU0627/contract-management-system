package com.contracthub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contracthub.entity.ContractReminder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ContractReminderMapper extends BaseMapper<ContractReminder> {
    @Select("""
            <script>
            SELECT cr.*
            FROM contract_reminder cr
            INNER JOIN (
                SELECT MAX(id) AS id
                FROM contract_reminder
                WHERE recipient_user_id = #{userId}
                <if test="keyword != null and keyword != ''">
                    AND (contract_no LIKE CONCAT('%', #{keyword}, '%')
                         OR contract_title LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
                GROUP BY CASE
                    WHEN contract_id IS NULL THEN CONCAT('id:', id)
                    ELSE CONCAT('contract:', contract_id)
                END
            ) latest ON latest.id = cr.id
            ORDER BY cr.created_at DESC, cr.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ContractReminder> selectMyDeduplicatedPage(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM (
                SELECT MAX(id) AS id
                FROM contract_reminder
                WHERE recipient_user_id = #{userId}
                <if test="keyword != null and keyword != ''">
                    AND (contract_no LIKE CONCAT('%', #{keyword}, '%')
                         OR contract_title LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
                GROUP BY CASE
                    WHEN contract_id IS NULL THEN CONCAT('id:', id)
                    ELSE CONCAT('contract:', contract_id)
                END
            ) t
            </script>
            """)
    long countMyDeduplicated(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM (
                SELECT MAX(id) AS id
                FROM contract_reminder
                WHERE recipient_user_id = #{userId}
                  AND (is_read = 0 OR is_read IS NULL)
                GROUP BY CASE
                    WHEN contract_id IS NULL THEN CONCAT('id:', id)
                    ELSE CONCAT('contract:', contract_id)
                END
            ) t
            </script>
            """)
    long countMyUnreadDeduplicated(@Param("userId") Long userId);

    @Select("""
            SELECT cr.*
            FROM contract_reminder cr
            INNER JOIN (
                SELECT MAX(id) AS id
                FROM contract_reminder
                GROUP BY CASE
                    WHEN contract_id IS NULL THEN CONCAT('id:', id)
                    ELSE CONCAT('contract:', contract_id)
                END
            ) latest ON latest.id = cr.id
            ORDER BY cr.created_at DESC, cr.id DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<ContractReminder> selectAllDeduplicatedPage(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select("""
            SELECT COUNT(1)
            FROM (
                SELECT MAX(id) AS id
                FROM contract_reminder
                GROUP BY CASE
                    WHEN contract_id IS NULL THEN CONCAT('id:', id)
                    ELSE CONCAT('contract:', contract_id)
                END
            ) t
            """)
    long countAllDeduplicated();
}
