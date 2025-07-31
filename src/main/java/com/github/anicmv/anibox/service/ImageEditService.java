package com.github.anicmv.anibox.service;

import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.entity.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anicmv
 * @date 2025/3/15 22:55
 * @description 相册访问服务
 */
@Service
@Log4j2
public class ImageEditService extends ImageService {

    public ResponseEntity<JSONObject> edit(String urls,
                                           String ids,
                                           String tags,
                                           Authentication auth) {

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 获取指定图片列表
        List<Image> imageList = super.getImageList(ids, urls);
        if (imageList.isEmpty()) {
            return super.error("No images found");
        }

        // 设置标签
        List<Tag> tagList = super.saveTags(tags);

        // 获取所有图片ID，方便删除原有关联数据
        List<Long> imageIds = imageList.stream()
                .map(Image::getId)
                .collect(Collectors.toList());

        // 删除这些图片原有的 image_tag 记录
        super.deleteImageTag(imageIds);

        // 构造新的关联数据
        List<ImageTag> imageTagList = new ArrayList<>();

        imageList.forEach(image -> tagList.forEach(tag ->
                imageTagList.add(
                        ImageTag.builder().imageId(image.getId()).tagId(tag.getId()).build()
                )
        ));

        // 插入新的关联数据
        super.saveTagList(imageTagList);

        return super.success();
    }

}
