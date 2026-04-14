package com.learn.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * UserClient 降级工厂（推荐方式）
 *
 * 相比直接实现 UserClient 的 Fallback，FallbackFactory 的优势：
 * 1. 可以捕获具体的异常信息（超时、拒绝连接、500 等）
 * 2. 根据不同异常类型做不同的降级处理
 * 3. 更方便记录日志和排查问题
 *
 * Sentinel 和 Resilience4j 两套方案共用这个 FallbackFactory
 *
 * @author MangoPie
 */
@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        // 打印异常信息，方便排查
        log.error("user-service 调用失败，触发降级。异常类型: {}, 异常信息: {}",
                cause.getClass().getSimpleName(), cause.getMessage());

        return new UserClient() {
            @Override
            public Map<String, Object> getUser(Long userId) {
                // 根据异常类型做差异化降级
                String reason = switch (cause.getClass().getSimpleName()) {
                    case "SocketTimeoutException" -> "用户服务响应超时";
                    case "ConnectException" -> "用户服务连接失败（服务可能已下线）";
                    case "CircuitBreakerOpenException", "CircuitBreakerStateException" -> "用户服务熔断器已打开";
                    default -> "用户服务暂时不可用";
                };

                log.warn("降级处理 - userId: {}, 降级原因: {}", userId, reason);

                return Map.of(
                        "code", 503,
                        "message", reason + "，请稍后重试",
                        "data", ""
                );
            }
        };
    }
}
