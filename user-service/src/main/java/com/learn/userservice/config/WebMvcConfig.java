package com.learn.userservice.config;

import com.learn.userservice.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置
 * 注册鉴权拦截器，配置拦截和排除的路径
 *
 * 拦截规则：
 * - 拦截：/user/** 下的所有接口（需要登录）
 * - 排除：/auth/** 下的登录接口（不需要登录）
 *
 * @author MangoPie
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/user/**")       // 拦截用户相关接口
                .excludePathPatterns("/auth/**");   // 排除登录接口
    }
}
