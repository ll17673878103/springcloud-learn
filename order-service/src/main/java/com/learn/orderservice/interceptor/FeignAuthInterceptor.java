package com.learn.orderservice.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 核心功能：自动将当前请求的 Authorization Header 转发给下游服务
 *
 * 工作原理：
 * 1. 用户请求 order-service 时带了 Token（Authorization: Bearer xxx）
 * 2. order-service 通过 Feign 调用 user-service
 * 3. 这个拦截器在 Feign 发请求前，自动把 Token 塞到转发请求的 Header 里
 * 4. user-service 的 AuthInterceptor 就能验证这个 Token
 *
 * 这就是微服务间 Token 传递的关键！
 *
 * @author MangoPie
 */
@Slf4j
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 1. 获取当前 HTTP 请求（用户发给 order-service 的请求）
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("无法获取当前请求上下文，跳过 Token 传递");
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 2. 从当前请求中取出 Authorization Header
        String authorization = request.getHeader("Authorization");

        if (authorization != null && !authorization.isEmpty()) {
            // 3. 将 Token 传递给 Feign 的转发请求
            template.header("Authorization", authorization);
            log.debug("Feign 请求已携带 Authorization Header");
        } else {
            log.warn("当前请求中没有 Authorization Header，Feign 转发将不带 Token");
        }
    }
}
