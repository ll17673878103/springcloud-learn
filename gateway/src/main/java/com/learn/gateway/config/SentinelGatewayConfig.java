package com.learn.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sentinel 网关限流配置
 *
 * 和 order-service 里的 Sentinel 不同：
 * - order-service 的 Sentinel：保护"服务间调用"（Feign 调用）
 * - Gateway 的 Sentinel：保护"外部请求入口"（客户端 → Gateway）
 *
 * 这里配置了路由级别的限流规则：
 * - 按 route ID 限流（限制每个路由的 QPS）
 *
 * @author MangoPie
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelGatewayConfig(
            ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * 配置 Sentinel 异常处理器
     * 当请求被限流时，返回自定义的 JSON 响应（而不是默认的错误页面）
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 配置 Sentinel 网关过滤器
     * 这个过滤器会拦截所有请求，检查是否触发限流规则
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 初始化限流规则和自定义限流响应
     */
    @PostConstruct
    public void initGatewayRules() {
        // 1. 配置限流规则
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 限制 user-service 路由：每秒最多 5 个请求
        // route ID 对应 application.yml 中的路由 ID
        rules.add(new GatewayFlowRule("user-service-routes")
                .setCount(5)        // QPS 阈值：每秒 5 次
                .setIntervalSec(1)  // 统计时间窗口：1 秒
        );

        // 限制 order-service 路由：每秒最多 3 个请求
        rules.add(new GatewayFlowRule("order-service-route")
                .setCount(3)        // QPS 阈值：每秒 3 次
                .setIntervalSec(1)  // 统计时间窗口：1 秒
        );

        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel Gateway 限流规则加载完成，共 {} 条", rules.size());

        // 2. 配置自定义限流响应（被限流时返回的 JSON）
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                log.warn("请求被限流，路径: {}", exchange.getRequest().getURI().getPath());
                return ServerResponse
                        .status(HttpStatus.TOO_MANY_REQUESTS)  // 429 状态码
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(
                                "{\"code\":429,\"message\":\"请求太频繁，请稍后再试\"}"
                        ));
            }
        });
    }
}
