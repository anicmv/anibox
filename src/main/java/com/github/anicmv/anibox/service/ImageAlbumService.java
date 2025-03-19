package com.github.anicmv.anibox.service;

import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.mapper.ImageMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author anicmv
 * @date 2025/3/15 22:55
 * @description 相册访问服务
 */
@Service
@Log4j2
public class ImageAlbumService extends ImageService {

    @Resource
    private ImageMapper imageMapper;

    public ResponseEntity<JSONObject> editAlbum(String urls, String ids, String album) {

        List<Image> imageList = super.getImageList(ids, urls);
        // 设置相册
        imageList.forEach(image -> image.setAlbum(album));

        imageMapper.updateById(imageList);
        return super.success();
    }
}
