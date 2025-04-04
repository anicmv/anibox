package com.github.anicmv.anibox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.anicmv.anibox.entity.ImageTag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author anicmv
 * @date 2025/2/27 15:29
 */
public interface ImageTagMapper extends BaseMapper<ImageTag> {
    @Insert({
            "<script>",
            "INSERT INTO image_tag (image_id, tag_id) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.imageId}, #{item.tagId})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE tag_id = VALUES(tag_id)",
            "</script>"
    })
    int insertOrUpdateBatch(@Param("list") List<ImageTag> list);
}
