package com.github.anicmv.anibox.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.mapper.ImageMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anicmv
 * @date 2025/3/15 22:55
 * @description 图片时间列表服务
 */
@Service
@Log4j2
public class ImageTimeLineService extends ImageService {

    @Resource
    private ImageMapper imageMapper;


    public ResponseEntity<JSONObject> timeline(
            String album,
            String startTime,
            String endTime,
            Integer page,
            Integer size
    ) {

        List<Image> imageList = getImageList(album, startTime, endTime, page, size);

        List<JSONObject> imageJsonList = new ArrayList<>();
        imageList.forEach(image -> imageJsonList.add(super.getReturnData(image)));

        return super.success(imageJsonList);
    }


    private List<Image> getImageList(String album, String startTime, String endTime, Integer page, Integer size) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(album)) {
            queryWrapper.eq("album", album);
        }
        // 添加开始时间条件（大于等于）
        if (ObjectUtil.isNotEmpty(startTime)) {
            queryWrapper.ge("created_at", startTime);
        }
        // 添加结束时间条件（小于等于）
        if (ObjectUtil.isNotEmpty(endTime)) {
            queryWrapper.le("created_at", endTime);
        }

        if (ObjectUtil.isEmpty(page)) {
            page = 0;
        }
        if (ObjectUtil.isEmpty(size)) {
            size = 50;
        }
        Page<Image> pageParam = new Page<>(page, size);
        IPage<Image> imagePage = imageMapper.selectPage(pageParam, queryWrapper);
        return imagePage.getRecords();
    }


}
