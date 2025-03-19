package com.github.anicmv.anibox.controller;

import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.service.JwtService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author anicmv
 * @date 2025/3/7 15:10
 * @description jwt注册控制器
 */
@RestController
public class JwtController {

    @Resource
    private JwtService jwtService;

    @PostMapping("/token")
    public JSONObject generateToken(String username, String password, String path) {
        return jwtService.generateToken(username, password, path);
    }
}
