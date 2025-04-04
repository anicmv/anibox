package com.github.anicmv.anibox.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.mapper.ImageMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            String albums,
            String tags,
            String startTime,
            String endTime,
            Integer page,
            Integer size
    ) {
        // 参数校验，避免空指针
        albums = (albums == null) ? "" : albums;
        tags = (tags == null) ? "" : tags;

        // 拆分相册和标签字符串，去空格、去重
        List<String> albumList = Arrays.stream(albums.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        List<String> tagList = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        // 使用 MyBatis-Plus 内置分页插件查询图片数据
        Page<Image> pageResult = getImageList(albumList, tagList, startTime, endTime, page, size);

        // 根据查询结果组装返回 JSON 数据
        List<JSONObject> imageJsonList = new ArrayList<>();
        pageResult.getRecords().forEach(image ->
                imageJsonList.add(super.getReturnData(image))
        );

        return super.success(imageJsonList);
    }

    private Page<Image> getImageList(List<String> albumList, List<String> tagList, String startTime, String endTime, Integer page, Integer size) {
        QueryWrapper<Image> qw = new QueryWrapper<>();

        // 添加相册条件：仅在 albumList 非空时添加
        if (!albumList.isEmpty()) {
            String albumInClause = albumList.stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(","));
            qw.inSql("id", "SELECT ia.image_id FROM image_album ia " +
                    "JOIN album a ON ia.album_id = a.id " +
                    "WHERE a.name IN (" + albumInClause + ")");
        }

        // 添加标签条件：仅在 tagList 非空时添加
        if (!tagList.isEmpty()) {
            String tagInClause = tagList.stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(","));
            qw.inSql("id", "SELECT it.image_id FROM image_tag it " +
                    "JOIN tag t ON it.tag_id = t.id " +
                    "WHERE t.name IN (" + tagInClause + ")");
        }

        // 添加时间过滤条件
        if (startTime != null && !startTime.trim().isEmpty()) {
            qw.ge("create_time", startTime);
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            qw.le("create_time", endTime);
        }

        if (ObjectUtil.isEmpty(page)) {
            page = 0;
        }
        if (ObjectUtil.isEmpty(size)) {
            size = 50;
        }
        // 使用 MyBatis-Plus 内置分页插件
        Page<Image> pageObj = new Page<>(page, size);
        return imageMapper.selectPage(pageObj, qw);
    }




}
