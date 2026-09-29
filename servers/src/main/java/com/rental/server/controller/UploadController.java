package com.rental.server.controller;

import com.rental.server.common.BusinessException;
import com.rental.server.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传接口：保存到本地目录，返回可直接访问的相对路径
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${rental.image-dir}")
    private String imageDir;

    @Value("${rental.image-url-prefix}")
    private String imageUrlPrefix;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }

        // 校验扩展名（防止上传非图片文件）
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "只支持 jpg / jpeg / png / gif / webp 格式的图片");
        }

        // 按日期分目录存放，避免单目录文件过多：2026/09/xxxx.jpg
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            Path dir = Paths.get(imageDir, datePath);
            Files.createDirectories(dir); // 目录不存在则创建
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());

            // 返回相对路径，前端拼上 baseURL 即可访问
            String url = imageUrlPrefix + "/" + datePath + "/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败: " + e.getMessage());
        }
    }
}