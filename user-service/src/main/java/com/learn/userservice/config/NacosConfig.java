package com.learn.userservice.config;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author MangoPie
 * @version 1.0
 * @data 2026/3/30 17:06
 */
@Data
@ConfigurationProperties
@NacosConfigurationProperties(dataId = "user-service-${spring.profiles.active}.yaml", autoRefreshed = true)
public class NacosConfig {

    private String env;

    private String version;

}