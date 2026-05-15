package com.learn.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 用户 Mapper
 *
 * @author MangoPie
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 扣减余额
     * 使用自定义 SQL，确保原子性
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @return 影响行数
     */
    @Update("UPDATE user SET balance = balance - #{amount} WHERE id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
