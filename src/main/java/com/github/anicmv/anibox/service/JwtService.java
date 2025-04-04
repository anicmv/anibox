package com.github.anicmv.anibox.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.anicmv.anibox.constent.ImageConstants;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.mapper.UserMapper;
import com.github.anicmv.anibox.utils.ImageUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

/**
 * @author anicmv
 * @date 2025/3/7 15:16
 * @description 用户注册服务
 */
@Service
public class JwtService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public JSONObject generateToken(String username, String password, String rootPath) {
        String passwordEncode = bCryptPasswordEncoder.encode(password);
        // 生成 JWT 密钥（Base64 格式的密钥字符串）
        String saltKey = ImageUtil.base64Key();
        // 生成token
        byte[] secretKey = Base64.getDecoder().decode(saltKey);

        //构建 JWT 载荷
        Map<String, Object> claims = Map.of("key", RandomUtil.randomString(16));

        String jwt = JWTUtil.createToken(claims, secretKey);
        User user = User.builder().username(username)
                .password(passwordEncode)
                .saltKey(saltKey)
                .token(SecureUtil.md5(jwt))
                .rootPath(rootPath)
                .roles(ImageConstants.USER)
                .build();

        saveOrUpdate(user);
        return JSONUtil.createObj().putOnce("token", jwt);
    }

    private void saveOrUpdate(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser == null) {
            userMapper.insert(user);
        } else {
            existingUser.setPassword(user.getPassword());
            existingUser.setSaltKey(user.getSaltKey());
            existingUser.setToken(user.getToken());
            existingUser.setRootPath(user.getRootPath());
            userMapper.updateById(existingUser);
        }
    }
}
