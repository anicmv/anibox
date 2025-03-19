package com.github.anicmv.anibox.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author anicmv
 * @date 2025/3/15 12:36
 * @description 图片文件上传服务
 */
@Log4j2
@Service
public class ImageUploadFileService extends ImageService {


    public ResponseEntity<JSONObject> uploadFile(
            MultipartFile[] files,
            String album,
            HttpServletRequest request,
            String aliasName,
            Authentication auth
    ) {

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


        if (files == null) {
            return super.error("Empty file");
        }
        List<MultipartFile> fileList = new ArrayList<>(Arrays.stream(files).toList());
        List<MultipartFile> toBeRemoved = new ArrayList<>();
        // 2.文件不存在
        if (fileList.isEmpty()) {
            return super.error("Empty file");
        }

        List<Image> imageList = new ArrayList<>();
        int size = fileList.size();
        fileList.forEach(file -> {
            try {
                // 3.文件重复
                Image image = super.findImage(file.getInputStream());
                if (image != null) {
                    imageList.add(image);
                    toBeRemoved.add(file);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        fileList.removeAll(toBeRemoved);
        // 4.自定义名字重复
        if (size == 1 && super.checkAliseName(aliasName)) {
            log.error("自定义文件名重复");
            aliasName = null;
        }
        if (size > 1) {
            aliasName = null;
        }

        String finalAliasName = aliasName;
        fileList.forEach(file -> {
            String originalFilename = file.getOriginalFilename();
            Image.ImageBuilder imageBuilder = super.getImageBuilder(album);
            String ipAddress = ImageUtil.extractClientIp(request);
            imageBuilder.uploadedIp(ipAddress);
            Image image = saveImage(file, user, imageBuilder, originalFilename, finalAliasName);
            imageList.add(image);
        });

        super.updateUserImageCount(user, fileList.size());
        // 3.存储图片
        return super.returnData(imageList);
    }


    private Image saveImage(MultipartFile file, User user, Image.ImageBuilder imageBuilder, String fileName, String aliasName) {
        // 获取文件后缀，并生成唯一的文件名
        String prefix = FileNameUtil.getPrefix(fileName);
        String suffix = FileNameUtil.getSuffix(fileName);

        super.setFileAttribute(file, imageBuilder);
        // 保存图片到本地磁盘
        File imageFile = super.getImageFile(user, suffix, imageBuilder);
        imageBuilder.userId(user.getId())
                .originName(prefix + "." + suffix)
                .name(imageFile.getName())
                .suffix(suffix);
        if (aliasName != null) {
            imageBuilder.aliasName(FileNameUtil.getPrefix(aliasName));
        }
        try {
            file.transferTo(imageFile);
        } catch (Exception e) {
            log.error(e);
        }
        return super.saveImageInfo(imageBuilder);
    }


}
