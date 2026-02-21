package com.gamestore.interceptor;

import com.alibaba.fastjson2.JSON;
import com.gamestore.utils.JwtUtil;
import com.gamestore.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 放行图片请求和文件上传
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/img/") || uri.startsWith("/api/upload/") || uri.equals("/api/file/upload")) {
            System.out.println("[AuthInterceptor] 放行路径: " + uri);
            return true;
        }
        
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(Result.error(401, "未登录或token已过期")));
            return false;
        }
        
        token = token.substring(7);
        
        try {
            jwtUtil.verify(token);
            // 将用户信息放入request
            Long userId = jwtUtil.getUserId(token);
            Integer role = jwtUtil.getRole(token);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(Result.error(401, "token无效或已过期")));
            return false;
        }
    }
}
