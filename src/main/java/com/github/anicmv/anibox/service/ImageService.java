package com.github.anicmv.anibox.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.anicmv.anibox.entity.Image;
import com.github.anicmv.anibox.entity.User;
import com.github.anicmv.anibox.enums.ImageEnum;
import com.github.anicmv.anibox.mapper.ImageMapper;
import com.github.anicmv.anibox.mapper.UserMapper;
import com.github.anicmv.anibox.utils.ImageUtil;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author anicmv
 * @date 2025/3/15 12:47
 * @description 图片主服务
 */
@Log4j2
@Service
public class ImageService {
    @Resource
    private ImageMapper imageMapper;

    @Resource
    private UserMapper userMapper;

    Image selectImageByMd5AndSha1(String md5, String sha1) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5", md5);
        queryWrapper.eq("sha1", sha1);
        return imageMapper.selectOne(queryWrapper);
    }


    Image findImage(InputStream is) {
        return selectImageByMd5AndSha1(DigestUtil.md5Hex(is), DigestUtil.sha1Hex(is));
    }


    Image.ImageBuilder getImageBuilder(String album) {
        Image.ImageBuilder imageBuilder = Image.builder();
        if (ObjectUtil.isNotEmpty(album)) {
            imageBuilder.album(album);
        }
        imageBuilder.shortKey(getShortKey())
                .createdAt(new Date())
                .updatedAt(new Date());
        return imageBuilder;
    }


    Image selectImageByShortKey(String shortKey) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("short_key", shortKey);
        return imageMapper.selectOne(queryWrapper);
    }


    String getShortKey() {
        String shortKey = ImageUtil.generateUniqueKey(8);
        Image image = selectImageByShortKey(shortKey);
        while (image != null) {
            shortKey = ImageUtil.generateUniqueKey(8);
            image = selectImageByShortKey(shortKey);
        }
        return shortKey;
    }

    File getImageFile(User user, String suffix, Image.ImageBuilder imageBuilder) {
        Path imagePath = getImagePath(user, imageBuilder);
        String imageName = getImageName(suffix);
        imageBuilder.name(imageName);
        return imagePath.resolve(imageName).toFile();
    }


    /**
     * 获取图片路径
     * @param user 用户
     * @param imageBuilder image构建器
     */
    Path getImagePath(User user, Image.ImageBuilder imageBuilder) {
        String datePath = ImageUtil.datePath();
        imageBuilder.path(datePath);
        String path = user.getRootPath() + File.separator + datePath + File.separator;
        Path uploadPath = Paths.get(path);

        if (Files.notExists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        // root_path + image_path
        return uploadPath;
    }

    String getImageName(String suffix) {
        return DateUtil.current() + RandomUtil.randomString(RandomUtil.BASE_CHAR, 2) + "." + suffix;
    }

    Image selectImageByAliasName(String aliasName) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("alias_name", aliasName);
        return imageMapper.selectOne(queryWrapper);
    }


    boolean checkAliseName(String aliasName) {
        Image image = selectImageByAliasName(aliasName);
        return image != null;
    }

    Path getImagePath(User user, Image image) {
        String imagePath = user.getRootPath() + File.separator + image.getPath();
        return Paths.get(imagePath).resolve(image.getName()).normalize();
    }


    void setFileAttribute(MultipartFile file, Image.ImageBuilder imageBuilder) {
        long size = file.getSize();
        imageBuilder.size(BigDecimal.valueOf(size));
        try {
            setWidthAndHeight(file, imageBuilder);
            // 计算文件大小（KB）
            double sizeKb = file.getSize() / 1024.0;
            imageBuilder.size(BigDecimal.valueOf(sizeKb));

            setMd5AndSha1(file.getInputStream(), imageBuilder);
        } catch (IOException e) {
            log.error(e);
        }
    }


    void setWidthAndHeight(File file, Image.ImageBuilder imageBuilder) {
        BufferedImage bufferedImage = ImgUtil.read(file);
        setWidthAndHeight(bufferedImage, imageBuilder);
    }

    private void setWidthAndHeight(BufferedImage bufferedImage, Image.ImageBuilder imageBuilder) {
        if (bufferedImage == null) {
            log.error("无法读取上传的图片文件");
            return;
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        imageBuilder.width(width).height(height);
    }


    void setWidthAndHeight(MultipartFile file, Image.ImageBuilder imageBuilder) throws IOException {
        BufferedImage bufferedImage = ImgUtil.read(new ByteArrayInputStream(file.getBytes()));
        setWidthAndHeight(bufferedImage, imageBuilder);
    }

    void setMd5AndSha1(InputStream is, Image.ImageBuilder imageBuilder) {
        String md5 = DigestUtil.md5Hex(is);
        String sha1 = DigestUtil.sha1Hex(is);
        imageBuilder.md5(md5).sha1(sha1);
    }


    Image saveImageInfo(Image.ImageBuilder imageBuilder) {
        Image image = imageBuilder.build();
        imageMapper.insert(image);
        // 返回存储位置的访问路径（实际情况需要配合静态资源映射或另外提供图片访问接口）
        return image;
    }


    Image queryImageByLike(String name) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name)
                .or().like("short_key", FileNameUtil.getPrefix(name))
                .or().like("alias_name", FileNameUtil.getPrefix(name));
        return imageMapper.selectOne(queryWrapper);
    }


    ResponseEntity<JSONObject> success(Image image) {
        JSONObject result = JSONUtil.createObj()
                .putOpt("result", ImageEnum.SUCCESS.getResult())
                .putOnce("code", ImageEnum.SUCCESS.getCode())
                .putOnce("data", getReturnData(image));
        return ResponseEntity.ok().body(result);
    }


    ResponseEntity<JSONObject> success(List<JSONObject> imageJsonList) {
        JSONObject result = JSONUtil.createObj()
                .putOpt("result", ImageEnum.SUCCESS.getResult())
                .putOnce("code", ImageEnum.SUCCESS.getCode())
                .putOnce("data", imageJsonList);
        return ResponseEntity.ok().body(result);
    }


    JSONObject getReturnData(Image image) {
        String url = getUrl(image.getName());
        JSONObject data = JSONUtil.createObj()
                .putOpt("id", image.getId())
                .putOpt("shortUrl", getUrl(image.getShortKey() + "." + image.getSuffix()))
                .putOnce("url", url)
                .putOnce("originName", image.getOriginName())
                .putOnce("album", image.getAlbum())
                .putOnce("size", image.getSize())
                .putOnce("md5", image.getMd5())
                .putOnce("sha1", image.getSha1())
                .putOnce("width", image.getWidth())
                .putOnce("height", image.getHeight())
                .putOnce("markdown", ImageUtil.markdownStr(image.getOriginName(), url))
                .putOnce("html", ImageUtil.htmlStr(image.getOriginName(), url))
                .putOnce("bbcode", ImageUtil.bbcodeStr(url));
        if (image.getAliasName() != null) {
            data.putOnce("aliasUrl", getUrl(image.getAliasName() + "." + image.getSuffix()));
        }
        return data;
    }


    ResponseEntity<JSONObject> error(String msg) {
        JSONObject result = JSONUtil.createObj()
                .putOpt("result", ImageEnum.FAILURE.getResult())
                .putOnce("code", ImageEnum.FAILURE.getCode())
                .putOnce("data", msg);
        return ResponseEntity.ok().body(result);
    }

    ResponseEntity<JSONObject> success() {
        JSONObject result = JSONUtil.createObj()
                .putOpt("result", ImageEnum.SUCCESS.getResult())
                .putOnce("code", ImageEnum.SUCCESS.getCode())
                .putOnce("data", "ok");
        return ResponseEntity.ok().body(result);
    }


    private String getUrl(String name) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/i/")
                .path(name)
                .toUriString();
    }


    List<Image> getImageList(String ids, String urls) {
        List<Image> imageList = new ArrayList<>();
        // 1.根据id删除
        if (ObjectUtil.isNotEmpty(ids)) {
            List<Image> images = imageMapper.selectByIds(Arrays.asList(ids.split(",")));
            imageList.addAll(images);
        }

        if (ObjectUtil.isNotEmpty(urls)) {
            Arrays.asList(urls.split(","))
                    .forEach(u -> {
                        String suffix = FileNameUtil.getSuffix(u);
                        String prefix = FileNameUtil.getPrefix(u);
                        imageList.add(queryImageByLike(prefix + "." + suffix));
                    });
        }
        return imageList;
    }


    Image randomImage() {
        QueryWrapper<Image> wrapper = new QueryWrapper<>();
        wrapper.like("album", "random");
        List<Image> images = imageMapper.selectList(wrapper);
        if (ObjectUtil.isEmpty(images)) {
            return null;
        }
        int size = images.size();
        return images.get(RandomUtil.randomInt(0, size));
    }


    ResponseEntity<JSONObject> returnData(List<Image> images) {
        List<JSONObject> imageJsonList = new ArrayList<>();
        images.forEach(image -> imageJsonList.add(getReturnData(image)));
        //if (imageJsonList.size() == 1) {
        //    return success(images.getFirst());
        //}
        return success(imageJsonList);
    }


    void updateUserImageCount(User user, int count) {
        // 用户表更新图片数量
        user.setImageNum(user.getImageNum() + count);
        userMapper.updateById(user);
    }


    @Resource(name = "binaryRedisTemplate")
    private RedisTemplate<String, byte[]> binaryRedisTemplate;

    InputStream readFromRedis(String countName, String imageName) {
        // 从 Redis 获取图片的字节数组
        byte[] retrievedImageData = binaryRedisTemplate.opsForValue().get(imageName);
        if (retrievedImageData == null) {
            return null;
        }
        redisTemplate.expire(countName, 1, TimeUnit.DAYS);
        binaryRedisTemplate.expire(imageName, 1, TimeUnit.DAYS);
        return new ByteArrayInputStream(retrievedImageData);
    }


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    void writeImageToRedis(String imageName, Path filePath) {
        String name = filePath.toFile().getName();
        writeToRedis(name, imageName, filePath, null);
    }


    private void writeToRedis(String countName, String imageName, Path filePath, byte[] imageByte) {
        // 每次从本地读取图片时，在redis中记录读取次数
        Long count = redisTemplate.opsForValue().increment(countName, 1);
        redisTemplate.expire(countName, 1, TimeUnit.DAYS);
        if (count == null) {
            count = 1L;
        }
        // 当读取次数超过200时，将图片的二进制形式存入redis
        if (count > 200) {
            try {
                byte[] binaryData;
                if (imageByte == null) {
                    binaryData = Files.readAllBytes(filePath);
                } else {
                    binaryData = imageByte;
                }

                binaryRedisTemplate.opsForValue().set(imageName, binaryData);
                binaryRedisTemplate.expire(imageName, 1, TimeUnit.DAYS);
                log.info("Stored binary image {} in redis.", filePath.getFileName());
            } catch (IOException ex) {
                log.error("Error reading binary image for redis storage: {}", ex.getMessage());
            }
        }
    }

    void writeAvatarToRedis(String countName, String imageName, byte[] imageByte) {
        writeToRedis(countName, imageName, null, imageByte);
    }
}
