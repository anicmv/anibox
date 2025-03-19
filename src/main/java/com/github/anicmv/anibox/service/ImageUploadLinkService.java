package com.github.anicmv.anibox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.utils.ImageUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author anicmv
 * @date 2025/3/15 12:36
 * @description 图片链接上传服务
 */
@Log4j2
@Service
public class ImageUploadLinkService extends ImageService {

    public ResponseEntity<JSONObject> uploadLink(String imageUrls, String album
            , HttpServletRequest request, String aliasName, Authentication auth) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<String> imageUrlList = Arrays.stream(imageUrls.split(",")).toList();

        if (imageUrlList.isEmpty()) {
            return super.error("Empty file");
        }

        List<Image> imageList = new ArrayList<>();
        List<Image> toBeRemoved = new ArrayList<>();
        int size = imageUrlList.size();

        if (size == 1 && super.checkAliseName(aliasName)) {
            log.error("自定义文件名重复");
            aliasName = null;
        }

        if (size > 1) {
            aliasName = null;
        }

        for (String imageUrl : imageUrlList) {
            HttpURLConnection connection = ImageUtil.getImageLinkConnection(imageUrl);
            if (connection == null) {
                return super.error("Failed to download image, http connection error");
            }
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return super.error("Failed to download image, HTTP response code:" + responseCode);
                }
                Image.ImageBuilder imageBuilder = super.getImageBuilder(album);
                String ipAddress = ImageUtil.extractClientIp(request);
                imageBuilder.uploadedIp(ipAddress);
                InputStream is = connection.getInputStream();
                // 1.先保存文件
                File imageFile = saveImage(is, user, imageBuilder, imageUrl, aliasName);
                // 3.文件重复
                Image image = super.findImage(FileUtil.getInputStream(imageFile));
                if (image != null) {
                    // 删除文件
                    FileUtil.del(imageFile);
                    imageList.add(image);
                    toBeRemoved.add(image);
                    continue;
                }
                // 3.存储图片
                imageList.add(saveImage(imageBuilder, imageFile));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        super.updateUserImageCount(user, imageList.size() - toBeRemoved.size());
        return super.returnData(imageList);
    }

    private File saveImage(InputStream is, User user, Image.ImageBuilder imageBuilder, String imageUrl, String aliasName) throws IOException {
        long size = is.available();
        String prefix = FileNameUtil.getPrefix(imageUrl);
        String suffix = FileNameUtil.getSuffix(imageUrl);
        // 计算文件大小（KB）
        imageBuilder.userId(user.getId())
                .suffix(suffix)
                .originName(prefix + "." + suffix)
                .size(BigDecimal.valueOf(size / 1024));

        if (aliasName != null) {
            imageBuilder.aliasName(FileNameUtil.getPrefix(aliasName));
        }

        // 保存图片到本地磁盘
        File imageFile = super.getImageFile(user, suffix, imageBuilder);
        Files.copy(is, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return imageFile;
    }

    private Image saveImage(Image.ImageBuilder imageBuilder, File imageFile) throws IOException {
        super.setWidthAndHeight(imageFile, imageBuilder);
        // 保存图片到本地磁盘
        super.setMd5AndSha1(FileUtil.getInputStream(imageFile), imageBuilder);
        return saveImageInfo(imageBuilder);
    }


}