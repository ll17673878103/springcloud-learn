package com.learn.userservice.interceptor;

import com.learn.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器
 * 拦截需要登录的请求，验证 Authorization Header 中的 Token
 *
 * 流程：
 * 1. 从请求头取出 Authorization
 * 2. 去掉 "Bearer " 前缀，得到 Token
 * 3. 解析 Token，验证有效性
 * 4. 将用户信息存入 request attribute，供 Controller 使用
 *
 * @author MangoPie
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少 Authorization 请求头，路径: {}", request.getRequestURI());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先获取 Token\"}");
            return false;
        }

        // 2. 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        try {
            // 3. 解析并验证 Token
            var claims = JwtUtil.parseToken(token);

            // 4. 将用户信息存入 request，供后续使用
            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("username", claims.getSubject());

            log.debug("鉴权通过，用户: {}, 路径: {}", claims.getSubject(), request.getRequestURI());
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token 已过期，请重新登录\"}");
            return false;

        } catch (Exception e) {
            log.warn("Token 无效: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token 无效\"}");
            return false;
        }
    }
}
