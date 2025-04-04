package com.github.anicmv.anibox.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.github.anicmv.anibox.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author anicmv
 * @date 2025/3/5 15:58
 * @description 图片控制器
 */
@Log4j2
@RestController
public class ImageController {

    @Resource
    private ImageUploadLinkService imageUploadLinkService;

    @Resource
    private ImageUploadFileService imageUploadFileService;

    @Resource
    private ImageViewService imageViewService;

    @Resource
    private ImageDeleteService imageDeleteService;

    @Resource
    private ImageEditService imageEditService;

    @Resource
    private ImageTimeLineService imageTimeLineService;


    /**
     * 上传图片
     *
     * @param files   根据文件上传
     * @param auth    鉴权
     * @param albums  相册
     * @param tags    标签
     * @param urls    链接
     * @param request 请求
     * @param name    自定义名
     * @return 上传信息
     */
    @PostMapping("/upload")
    public ResponseEntity<JSONObject> uploadImage(
            MultipartFile[] files,
            String albums,
            String tags,
            Authentication auth,
            String urls,
            HttpServletRequest request,
            String name
    ) {

        if (ObjectUtil.isEmpty(urls)) {
            return imageUploadFileService.uploadFile(files, albums, tags, request, name, auth);
        }
        return imageUploadLinkService.uploadLink(urls, albums, tags, request, name, auth);
    }


    /**
     * 根据图片文件名称返回图片数据
     *
     * @param name 图片文件名称
     * @return 返回图片二进制数据流
     */
    @GetMapping("/i/{name}")
    public ResponseEntity<InputStreamResource> viewImage(
            @PathVariable String name,
            @RequestParam(required = false) Integer w,
            @RequestParam(required = false) Integer h,
            @RequestParam(required = false) Integer x,
            @RequestParam(required = false) Integer y
    ) {
        return imageViewService.viewImage(name, w, h, x, y);
    }


    /**
     * 根据图片文件名称返回圆形头像图片
     *
     * @param name 图片文件名称
     * @return 返回图片二进制数据流
     */
    @GetMapping("/a/{name}")
    public ResponseEntity<InputStreamResource> avatar(@PathVariable String name) {
        return imageViewService.avatar(name);
    }


    /**
     * 删除图片
     *
     * @param urls 链接
     * @param auth 鉴权
     * @param ids  id
     */

    @PostMapping("/delete")
    //@PreAuthorize("hasAnyAuthority(T(com.github.anicmv.anibox.constent.ImageConstants).ADMIN, T(com.github.anicmv.anibox.constent.ImageConstants).USER)")
    public ResponseEntity<JSONObject> deleteImage(String urls, String ids, Authentication auth) {
        return imageDeleteService.deleteImage(urls, ids, auth);
    }


    /**
     * 通过图片url或者图片id批量修改图片相册
     *
     * @param urls   链接
     * @param ids    鉴权
     * @param albums 相册
     * @param tags 标签
     */
    @PostMapping("/edit")
    public ResponseEntity<JSONObject> edit(String urls, String ids, String albums, String tags, Authentication auth) {
        return imageEditService.edit(urls, ids, albums, tags, auth);
    }


    /**
     * 获取图片列表
     *
     * @param albums    相册
     * @param tags      标签
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param page      页
     * @param size      每页数量
     * @return 图片列表
     */
    @PostMapping("/timeline")
    public ResponseEntity<JSONObject> timeline(
            String albums,
            String tags,
            String startTime,
            String endTime,
            Integer page,
            Integer size
    ) {
        return imageTimeLineService.timeline(albums, tags, startTime, endTime, page, size);
    }


    /**
     * 随机图片
     *
     * @return 图片
     */
    @GetMapping("/random")
    public ResponseEntity<InputStreamResource> randomImage() {
        return imageViewService.random();
    }
}
