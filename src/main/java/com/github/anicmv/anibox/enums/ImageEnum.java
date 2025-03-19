package com.github.anicmv.anibox.enums;

import lombok.Getter;

/**
 * @author anicmv
 * @date 2025/3/15 19:24
 * @description 图片返回参数枚举
 */
@Getter
public enum ImageEnum {

    SUCCESS("success", 200),
    FAILURE("failure", -1);


    private final String result;
    private final Integer code;


    ImageEnum(String result, Integer code) {
        this.result = result;
        this.code = code;
    }
}
