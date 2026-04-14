package com.learn.userservice.listener;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

/**
 * Nacos 配置服务 - 初始化监听器
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.cloud.nacos.server-addr")
public class NacosConfigService {

    @Value("${spring.cloud.nacos.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.config.namespace:}")
    private String namespace;

    // 从配置中动态获取，不要写死
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${spring.cloud.nacos.config.file-extension:yaml}")
    private String fileExtension;

    // Group 也从配置中读取，不写死
    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String group;

    @Autowired
    private NacosConfigListener nacosConfigListener;

    @PostConstruct
    public void init() {
        try {
            // 动态构建 DATA_ID: applicationName-profiles-active.fileExtension
            String dataId = buildDataId();
            log.info("========== Nacos 配置监听器初始化 ==========");
            log.info("应用名称: {}", applicationName);
            log.info("激活环境: {}", activeProfile);
            log.info("分组: {}", group);
            log.info("文件格式: {}", fileExtension);
            log.info("监听 Data ID: {}", dataId);

            // 获取 ConfigService
            ConfigService configService = createConfigService();

            // 先获取一次配置
            String config = configService.getConfig(dataId, group, 5000);
            log.info("当前配置内容: {}", config);

            // 添加监听器
            configService.addListener(dataId, group, nacosConfigListener);

            log.info("Nacos 配置监听器初始化成功");
            log.info("==========================================");

        } catch (NacosException e) {
            log.error("Nacos 配置监听器初始化失败", e);
        }
    }

    /**
     * 动态构建 Data ID
     * 规则: ${spring.application.name}-${spring.profiles.active}.${file-extension}
     */
    private String buildDataId() {
        // 如果有激活的环境，格式为: user-service-pro
        // 如果没有激活的环境，格式为: user-service
        String dataId = applicationName;
        if (activeProfile != null && !activeProfile.isEmpty()) {
            dataId = applicationName + "-" + activeProfile;
        }
        // 添加文件后缀（不包含点）
        if (fileExtension != null && !fileExtension.isEmpty()) {
            dataId = dataId + "." + fileExtension;
        }
        return dataId;
    }

    private ConfigService createConfigService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }
        return NacosFactory.createConfigService(properties);
    }
}
