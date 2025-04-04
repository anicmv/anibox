package com.github.anicmv.anibox.filter;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author anicmv
 * @date 2025/2/23 22:29
 * @description token验证分流器
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/i/") || uri.startsWith("/a/")) {
            filterChain.doFilter(request, response);
        } else {
            // token 在 Authorization 请求头中，格式为 "Bearer <token>"
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                User user = validateToken(token);
                if (user != null) {
                    // 如果 token 验证成功
                    Authentication auth = getAuthentication(user, token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    filterChain.doFilter(request, response);
                }
            }
        }

    }

    @Resource
    private UserMapper userMapper;

    private User validateToken(String token) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", SecureUtil.md5(token));
        return userMapper.selectOne(queryWrapper);
    }


    private Authentication getAuthentication(User user, String token) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRoles()));
        }
        // 构建并返回 Authentication 对象
        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }
}