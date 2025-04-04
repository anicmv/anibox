package com.github.anicmv.anibox.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

/**
 * @author anicmv
 * @date 2025/3/10 15:44
 * @description 图片工具类
 */
@Log4j2
public class ImageUtil {

    /**
     * 生成8位字符串
     */
    public static String generateUniqueKey(int length) {
        return RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER_LOWER, length);
    }

    public static String datePath() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return today.format(formatter);
    }


    public static HttpURLConnection getImageLinkConnection(String imageUrl) {
        try {
            // 解析 URL，打开连接
            URL url = URI.create(imageUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
            connection.connect();
            return connection;
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }


    public static String extractClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // 如果通过反向代理有多个 IP ，则取第一个
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }


    public static String base64Key() {
        byte[] key = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }


    public static String markdownStr(String linkText, String url) {
        return StrUtil.format("![{}]({})", linkText, url);
    }


    public static String htmlStr(String linkText, String url) {
        return StrUtil.format("<img src=\"{}\" alt=\"{}\">", url, linkText);
    }

    public static String bbcodeStr(String url) {
        return StrUtil.format("[img]{}[/img]", url);
    }


    public static InputStreamResource handleImage(File file, Integer w, Integer h, Integer x, Integer y) {
        BufferedImage image = ImgUtil.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        Image targetImage = null;
        if (w != null || h != null) {
            if (w != null && h == null) {
                targetImage = ImgUtil.scale(image, (float) w / width);
            } else if (w == null) {
                targetImage = ImgUtil.scale(image, (float) h / height);
            } else {
                float ww = (float) w / width;
                float hh = (float) h / height;
                float scale = Math.max(ww, hh);
                targetImage = ImgUtil.scale(image, scale);
                int targetImageWidth = targetImage.getWidth(null);
                int targetImageHeight = targetImage.getHeight(null);
                int startX = 0;
                int startY = 0;
                if (ww > hh) {
                    startY = (targetImageHeight - h) / 2;
                } else {
                    startX = (targetImageWidth - w) / 2;
                }
                Rectangle rectangle = new Rectangle(startX, startY, w, h);
                targetImage = ImgUtil.cut(targetImage, rectangle);
            }
        }
        if (targetImage == null) {
            targetImage = image;
        }

        if (x != null || y != null) {
            Rectangle rectangle;
            int targetImageWidth = targetImage.getWidth(null);
            int targetImageHeight = targetImage.getHeight(null);
            if (x != null && y == null) {
                rectangle = new Rectangle(x, 0, targetImageWidth, targetImageHeight);
            } else {
                rectangle = new Rectangle(Objects.requireNonNullElse(x, 0), y, targetImageWidth, targetImageHeight);
            }
            targetImage = ImgUtil.cut(targetImage, rectangle);
        }
        byte[] imageByte = ImgUtil.toBytes(targetImage, FileNameUtil.getSuffix(file));
        ImgUtil.flush(image);
        return new InputStreamResource(new ByteArrayInputStream(imageByte));
    }

    public static byte[] avatarImage(File file) {
        BufferedImage bufferedImage = ImgUtil.read(file);
        Image image = ImgUtil.cut(bufferedImage, 0, 0);
        byte[] imageByte = ImgUtil.toBytes(image, FileNameUtil.getSuffix(file));
        ImgUtil.flush(image);
        return imageByte;
    }

}
