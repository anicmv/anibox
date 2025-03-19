package com.github.anicmv.anibox.service;

import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.mapper.ImageMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author anicmv
 * @date 2025/3/15 13:23
 * @description 图片删除服务
 */
@Service
@Log4j2
public class ImageDeleteService extends ImageService {
    @Resource
    private ImageMapper imageMapper;


    public ResponseEntity<JSONObject> deleteImage(String urls, String ids, Authentication auth) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Image> imageList = super.getImageList(ids, urls);


        imageList.forEach(image -> {
            Path imagePath = super.getImagePath(user, image);
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });

        return imageMapper.deleteByIds(imageList) > 0 ? super.success() : super.error("delete image error");
    }
}
