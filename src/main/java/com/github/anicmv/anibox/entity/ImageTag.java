package com.github.anicmv.anibox.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author anicmv
 * @date 2025/2/27 15:30
 * @description 用户实体类
 */
@Data
@Builder
public class ImageTag implements Serializable {

    /**
     * 图片ID
     */
    private Long imageId;

    /**
     * 标签ID
     */
    private Long tagId;


    private static final long serialVersionUID = 1L;
}