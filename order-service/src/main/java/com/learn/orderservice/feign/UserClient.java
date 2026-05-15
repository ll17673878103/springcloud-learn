package com.learn.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign 客户端接口
 * 声明式调用 user-service 的 API
 *
 * fallbackFactory vs fallback：
 * - fallback：只能兜底，拿不到异常信息
 * - fallbackFactory：可以捕获具体异常，根据异常类型做不同处理（推荐）
 *
 * @author MangoPie
 */
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    /**
     * 调用 user-service 的 /user/{id} 接口
     * 该接口需要 Token 鉴权
     */
    @GetMapping("/user/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long userId);

    /**
     * 调用 user-service 的扣款接口
     * 参与 Seata 分布式事务（RM 分支事务）
     */
    @GetMapping("/user/deduct")
    Map<String, Object> deductBalance(@RequestParam("userId") Long userId,
                                      @RequestParam("amount") BigDecimal amount);

}