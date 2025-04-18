package com.github.anicmv.anibox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author anicmv
 * @date 2025/2/27 15:30
 * @description 用户实体类
 */
@Data
@Builder
public class User implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色
     */
    private String roles;

    /**
     * jwt密钥
     */
    private String saltKey;

    /**
     * jwt
     */
    private String token;

    /**
     * 图片根目录
     */
    private String rootPath;

    /**
     * 图片数量
     */
    private Long imageNum;

    /**
     * 状态
     */
    private Byte status;

    // 创建时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createTime;
    // 更新时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp updateTime;

    private static final long serialVersionUID = 1L;
}