package com.learn.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置属性
 * 绑定 application.yml 中 app.* 前缀的配置
 *
 * @author MangoPie
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class NacosConfig {

    /**
     * 当前环境（local/dev/pro）
     */
    private String env;

    /**
     * 应用版本号
     */
    private String version;

}