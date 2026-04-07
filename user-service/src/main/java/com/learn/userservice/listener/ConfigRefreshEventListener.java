package com.learn.userservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 配置刷新事件监听器
 * 监听 @RefreshScope 刷新事件，用于主动通知
 */
@Slf4j
@Component
public class ConfigRefreshEventListener {

    @EventListener
    public void onRefreshScope(RefreshScopeRefreshedEvent event) {
        log.info("========== RefreshScope 刷新事件触发 ==========");
        log.info("来源: {}", event.getSource());
        
        // 通知后台管理系统
        notifyAdmin();
        
        log.info("========== 事件处理完成 ==========");
    }

    /**
     * 通知后台管理系统
     */
    private void notifyAdmin() {
        // 实际场景中，这里可以：
        // 1. 发送 WebSocket 通知
        // 2. 发送邮件/短信
        // 3. 调用后台管理系统的通知接口
        // 4. 发送到消息队列
        
        log.info("通知后台管理系统: 配置已更新");
    }
}
