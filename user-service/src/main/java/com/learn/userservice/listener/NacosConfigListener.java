package com.learn.userservice.listener;

import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * Nacos 配置监听器 - 监听配置变化并刷新缓存
 */
@Slf4j
@Component
public class NacosConfigListener implements Listener {

    private static final String CACHE_KEY_PREFIX = "user:cache:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("========== Nacos 配置变化检测到 ==========");
        log.info("配置内容: {}", configInfo);
        
        // 刷新 Redis 缓存
        refreshCache();
        
        log.info("========== 缓存刷新完成 ==========");
    }

    /**
     * 刷新 Redis 缓存
     */
    private void refreshCache() {
        try {
            // 清除用户列表缓存
            String userListKey = CACHE_KEY_PREFIX + "list";
            Boolean deleted = redisTemplate.delete(userListKey);
            log.info("清除缓存 key: {}, 结果: {}", userListKey, deleted);

            // 清除用户详情缓存（所有用户）
            String userDetailPattern = CACHE_KEY_PREFIX + "detail:*";
            redisTemplate.delete(redisTemplate.keys(userDetailPattern));
            log.info("已清除所有用户详情缓存");

            // 清除其他业务缓存
            clearBusinessCache();

        } catch (Exception e) {
            log.error("刷新缓存失败", e);
        }
    }

    /**
     * 清除业务缓存 - 根据具体业务扩展
     */
    private void clearBusinessCache() {
        // 扩展其他缓存清除逻辑
        log.info("业务缓存清除完成");
    }
}
