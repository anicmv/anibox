package com.github.anicmv.anibox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.anicmv.anibox.entity.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author anicmv
 * @date 2025/2/27 15:29
 */
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT DISTINCT t.* " +
            "FROM image i " +
            "INNER JOIN image_tag it ON i.id = it.image_id " +
            "INNER JOIN tag t ON t.id = it.tag_id " +
            "WHERE i.id = #{imageId}")
    List<Tag> selectDistinctTagNames(@Param("imageId") Long imageId);
}
