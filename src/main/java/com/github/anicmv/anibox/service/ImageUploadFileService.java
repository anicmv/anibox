package com.github.anicmv.anibox.service;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.entity.*;
import com.github.anicmv.anibox.utils.ImageUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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


    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<JSONObject> uploadFile(
            MultipartFile[] files,
            String tags,
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

        // 重复文件
        List<MultipartFile> toBeRemoved = new ArrayList<>();

        List<Image> imageList = new ArrayList<>();
        int size = fileList.size();

        // 存储tag
        List<Tag> tagList = tags != null ? super.saveTags(tags) : null;

        // 重复文件处理
        duplicateFileProcess(fileList, imageList, toBeRemoved);
        // 自定义名字重复处理
        String finalAliasName = aliseNameProcess(size, aliasName);

        // ImageTag
        List<ImageTag> imageTagList = new ArrayList<>();

        // 上传
        fileList.forEach(file -> {
            Image image = saveImage(request, file, user, finalAliasName);
            imageList.add(image);
        });

        // 有个bug 存量图片 返回数据不更新tag
        imageList.forEach(image -> {
            // 根据image查询tag
            if (tags != null) {
                tagList.forEach(tag -> {
                    ImageTag imageTag = ImageTag.builder()
                            .imageId(image.getId())
                            .tagId(tag.getId()).build();
                    imageTagList.add(imageTag);
                });
            }
        });

        // 保存ImageTag
        super.saveTagList(imageTagList);
        super.updateUserImageCount(user, fileList.size());
        // 返回数据携带tags
        return super.returnData(imageList);
    }


    /**
     * 重复文件处理
     *
     * @param fileList    文件列表
     * @param imageList   image列表
     * @param toBeRemoved 重复文件列表
     */
    private void duplicateFileProcess(List<MultipartFile> fileList, List<Image> imageList, List<MultipartFile> toBeRemoved) {
        fileList.forEach(file -> {
            try {
                Image image = super.findImage(file.getInputStream());
                if (image != null) {
                    // 文件重复
                    imageList.add(image);
                    toBeRemoved.add(file);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        // 剔除重复文件
        fileList.removeAll(toBeRemoved);
    }

    /**
     * 处理自定义文件名
     *
     * @param size      文件数量数量
     * @param aliasName 自定义文件名
     */
    private String aliseNameProcess(int size, String aliasName) {
        if (size == 1 && super.checkAliseName(aliasName)) {
            log.error("自定义文件名重复");
            aliasName = null;
        }
        // 不支持多文件自定义名
        if (size > 1) {
            aliasName = null;
        }

        return aliasName;
    }


    private Image saveImage(HttpServletRequest request, MultipartFile file, User user, String aliasName) {
        String originalFilename = file.getOriginalFilename();
        Image.ImageBuilder imageBuilder = Image.builder()
                .uploadedIp(ImageUtil.extractClientIp(request))
                .shortKey(getShortKey());
        // 获取文件后缀，并生成唯一的文件名
        String prefix = FileNameUtil.getPrefix(originalFilename);
        String suffix = FileNameUtil.getSuffix(originalFilename);

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
