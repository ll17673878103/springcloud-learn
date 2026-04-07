package com.learn.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 配置示例 Controller - 演示方式4（@RefreshScope + @Value）
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RefreshScope
public class ConfigDemoController {

    // 方式4：使用 @Value，配置变化自动刷新
    @Value("${user.cache.timeout:300}")
    private int cacheTimeout;

    @Value("${user.page.size:10}")
    private int pageSize;

    @Value("${user.feature.enabled:false}")
    private boolean featureEnabled;

    // 如果某个配置没有，可以设置默认值
    @Value("${user.custom.config:default_value}")
    private String customConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取当前配置值
     * Nacos 修改配置后，下次访问接口，值会自动更新
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("cacheTimeout", cacheTimeout);
        config.put("pageSize", pageSize);
        config.put("featureEnabled", featureEnabled);
        config.put("customConfig", customConfig);
        config.put("note", "Nacos 修改配置后，刷新浏览器即可获取新值");
        
        log.info("获取配置: {}", config);
        return config;
    }

    /**
     * 模拟从缓存获取数据
     * 演示缓存读取 + 配置使用
     */
    @GetMapping("/cache-demo")
    public Map<String, Object> getCacheDemo() {
        String cacheKey = "user:cache:list";
        
        // 先从 Redis 获取
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        Map<String, Object> result = new HashMap<>();
        
        if (cached != null) {
            result.put("source", "Redis缓存");
            result.put("data", cached);
            result.put("cacheTimeout", cacheTimeout);
        } else {
            // 模拟从数据库查询
            Map<String, Object> data = new HashMap<>();
            data.put("users", new String[]{"张三", "李四", "王五"});
            
            // 写入缓存，超时时间使用配置值
            redisTemplate.opsForValue().set(cacheKey, data, cacheTimeout, TimeUnit.SECONDS);
            
            result.put("source", "数据库");
            result.put("data", data);
            result.put("cacheTimeout", cacheTimeout);
        }
        
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("cacheTimeout", cacheTimeout);
        status.put("pageSize", pageSize);
        return status;
    }
}
