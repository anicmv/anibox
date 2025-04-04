package com.github.anicmv.anibox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author anicmv
 * @date 2025/2/27 15:30
 * @description 图片实体类
 */
@Data
@Builder
public class Image implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户
     */
    private Long userId;

    /**
     * key
     */
    private String shortKey;

    /**
     * 保存路径
     */
    private String path;

    /**
     * 保存名称
     */
    private String name;

    /**
     * 原始名称
     */
    private String originName;

    /**
     * 别名
     */
    private String aliasName;

    /**
     * 图片大小(kb)
     */
    private BigDecimal size;

    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 文件MD5
     */
    private String md5;

    /**
     * 文件SHA1
     */
    private String sha1;

    /**
     * 宽
     */
    private Integer width;

    /**
     * 高
     */
    private Integer height;

    /**
     * 访问权限
     */
    private Byte permission;

    /**
     * 上传IP
     */
    private String uploadedIp;

    // 创建时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createTime;
    // 更新时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp updateTime;

    private static final long serialVersionUID = 1L;
}