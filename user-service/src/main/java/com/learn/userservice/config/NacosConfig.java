package com.learn.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author MangoPie
 * @version 1.0
 * @data 2026/3/30 17:06
 */
@Data
@ConfigurationProperties()
public class NacosConfig {

    private String env;

    private String version;

}