package com.learn.gateway.filter;

import com.learn.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关统一鉴权过滤器
 *
 * 所有请求先经过这个"安检门"：
 * 1. 白名单路径直接放行（比如登录接口）
 * 2. 其他路径必须携带有效 Token
 * 3. Token 有效则将用户信息传递给下游服务
 *
 * @author MangoPie
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 白名单：这些路径不需要 Token 就能访问
     * Ant 风格匹配：* 匹配一层，** 匹配多层
     */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/login",        // 登录接口
            "/auth/**"            // 所有认证相关接口（预留注册等）
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取请求路径（去掉 StripPrefix 前缀之前的原始路径）
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 去掉 Gateway 的路由前缀（/api）再匹配白名单
        // 因为 StripPrefix=1 会把 /api 去掉再转发，白名单应该匹配去掉 /api 后的路径
        String realPath = path;
        if (path.startsWith("/api/")) {
            realPath = path.substring(4); // 去掉 "/api"
        }

        log.debug("Gateway 鉴权拦截，原始路径: {}, 实际路径: {}", path, realPath);

        // 2. 白名单路径直接放行
        if (isWhiteListed(realPath)) {
            log.debug("白名单路径，直接放行: {}", realPath);
            return chain.filter(exchange);
        }

        // 3. 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少 Authorization 请求头，路径: {}", path);
            return unauthorized(exchange.getResponse(), "未登录，请先获取 Token");
        }

        // 4. 提取并验证 Token
        String token = authHeader.substring(7); // 去掉 "Bearer "

        try {
            Claims claims = JwtUtil.parseToken(token);

            // 5. 将用户信息通过请求头传递给下游服务
            //    下游服务可以通过 @RequestHeader 获取
            String userId = String.valueOf(claims.get("userId", Long.class));
            String username = claims.getSubject();

            log.info("鉴权通过，用户: {}，路径: {}", username, path);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token 已过期，路径: {}", path);
            return unauthorized(exchange.getResponse(), "Token 已过期，请重新登录");

        } catch (Exception e) {
            log.warn("Token 无效，路径: {}，原因: {}", path, e.getMessage());
            return unauthorized(exchange.getResponse(), "Token 无效");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回 401 未授权响应
     *
     * 注意：Gateway 用的是 WebFlux，不能像 Servlet 那样直接 response.getWriter()。
     * 需要用 DataBuffer 写入响应体。
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = "{\"code\":401,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器优先级，数字越小优先级越高
     * 鉴权过滤器应该优先执行，设为最高优先级
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
