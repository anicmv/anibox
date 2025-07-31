package com.github.anicmv.anibox.service;

import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.mapper.UserMapper;
import com.github.anicmv.anibox.utils.ImageUtil;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author anicmv
 * @date 2025/3/15 13:21
 * @description 图片访问服务
 */
@Log4j2
@Service
public class ImageViewService extends ImageService {
    @Resource
    private UserMapper userMapper;

    public ResponseEntity<InputStreamResource> viewImage(String name, Integer w, Integer h, Integer x, Integer y) {
        // 从redis中读取,如果有直接返回
        String redisKey = "image_" + name;
        try (InputStream inputStream = super.readFromRedis(name, redisKey)) {
            if (inputStream != null) {
                InputStreamResource resource = new InputStreamResource(inputStream);
                return returnData(resource, Path.of(name), name);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        Image image = super.queryImageByLike(name);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = getPath(image);

        if (Files.notExists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            if (!(w == null && h == null && x == null && y == null)) {
                resource = ImageUtil.handleImage(filePath.toFile(), w, h, x, y);
            }

            // redis计数
            super.writeImageToRedis(redisKey, filePath);

            return returnData(resource, filePath, name);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


    public ResponseEntity<InputStreamResource> avatar(String name) {
        String countName = "avatar_" + name;
        String redisKey = "image_avatar_" + name;

        try (InputStream inputStream = super.readFromRedis(countName, redisKey)) {
            if (inputStream != null) {
                InputStreamResource resource = new InputStreamResource(inputStream);
                return returnData(resource, Path.of(name), name);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }


        Image image = super.queryImageByLike(name);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = getPath(image);

        if (Files.notExists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] imageByte = ImageUtil.avatarImage(filePath.toFile());
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(imageByte));
            // redis计数
            super.writeAvatarToRedis(countName, redisKey, imageByte);
            return returnData(resource, filePath, name);
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    public ResponseEntity<InputStreamResource> xp(String tags) {
        Image image = super.randomImage(tags);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = getPath(image);
        if (Files.notExists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            return returnData(resource, filePath, image.getName());
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    private ResponseEntity<InputStreamResource> returnData(InputStreamResource resource, Path filePath, String name) throws IOException {

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }


    private Path getPath(Image image) {
        User user = userMapper.selectById(image.getUserId());
        String imagePath = user.getRootPath() + File.separator + image.getPath();
        return Paths.get(imagePath).resolve(image.getName()).normalize();
    }

}
