package com.github.anicmv.anibox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.anicmv.anibox.entity.ImageAlbum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author anicmv
 * @date 2025/2/27 15:29
 */
public interface ImageAlbumMapper extends BaseMapper<ImageAlbum> {
    @Insert({
            "<script>",
            "INSERT INTO image_album (image_id, album_id) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.imageId}, #{item.albumId})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE album_id = VALUES(album_id)",
            "</script>"
    })
    int insertOrUpdateBatch(@Param("list") List<ImageAlbum> list);
}
