package com.learn.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 *
 * 记录每个经过 Gateway 的请求：
 * - 请求方法（GET/POST）
 * - 请求路径
 * - 响应状态码
 * - 请求耗时
 *
 * 执行顺序：order = 0，在鉴权过滤器（order = -1）之后执行
 *
 * @author MangoPie
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    /**
     * 请求开始时间的属性 key
     * 存在 exchange 的 attributes 里，Pre 和 Post 阶段共享
     */
    private static final String START_TIME_KEY = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // ===== Pre 阶段：记录开始时间 =====
        exchange.getAttributes().put(START_TIME_KEY, System.currentTimeMillis());

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String queryString = (query != null && !query.isEmpty()) ? "?" + query : "";

        log.info(">>> Gateway 收到请求: {} {}", method, path + queryString);

        // ===== Post 阶段：请求完成后记录耗时和状态码 =====
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME_KEY);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;

                ServerHttpResponse response = exchange.getResponse();
                String statusCode = response.getStatusCode() != null
                        ? String.valueOf(response.getStatusCode().value())
                        : "unknown";

                log.info("<<< Gateway 响应完成: {} {} | 状态码: {} | 耗时: {}ms",
                        method, path, statusCode, duration);
            }
        }));
    }

    /**
     * 执行顺序：0（鉴权过滤器是 -1，比它晚一点执行）
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
