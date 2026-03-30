package com.learn.userservice.controller;

import com.learn.userservice.config.NacosConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
//@RefreshScope    // ⬅️ 添加热更新支持
@Slf4j
@RequiredArgsConstructor
public class UserController {

    @Value("${env}")
    private String env;
    @Value("${version}")
    private String version;


    private final NacosConfig nacosConfig;


    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id) {
        return "User " + id;
    }

    @GetMapping("/user/list")
    public String listUsers() {
        return "User List: [1, 2, 3]";
    }

    @GetMapping("/confzig")
    public String getConfig() {
        log.info("env: {}, version: {}", env, version);
        return "env: " + env + ", version: " + version;
    }

    @GetMapping("/test")
    public String test() {
        log.info("nacosConfig: {env:{}, version:{}}", nacosConfig.getEnv(), nacosConfig.getVersion());
        return nacosConfig.getEnv() + " - " + nacosConfig.getVersion();
    }
}