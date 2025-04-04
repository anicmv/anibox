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
public class Tag implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    // 创建时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createTime;
    // 更新时间
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp updateTime;

    private static final long serialVersionUID = 1L;
}