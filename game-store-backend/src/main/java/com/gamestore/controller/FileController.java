package com.gamestore.controller;

import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        System.out.println("[FileController] 接收到上传请求");
        
        if (file.isEmpty()) {
            System.out.println("[FileController] 文件为空");
            return Result.error("文件为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        System.out.println("[FileController] 原始文件名: " + originalFilename);
        System.out.println("[FileController] 上传路径: " + uploadPath);
        
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;
        
        File dest = new File(uploadPath + newFilename);
        System.out.println("[FileController] 目标文件: " + dest.getAbsolutePath());
        
        if (!dest.getParentFile().exists()) {
            System.out.println("[FileController] 创建目录: " + dest.getParentFile().getAbsolutePath());
            dest.getParentFile().mkdirs();
        }
        
        try {
            file.transferTo(dest);
            System.out.println("[FileController] 文件上传成功: " + newFilename);
            Map<String, String> data = new HashMap<>();
            // 使用 /api/img/ 路径，通过 ImageController 访问，绕过拦截器
            data.put("url", "/api/img/" + newFilename);
            data.put("filename", newFilename);
            return Result.success("上传成功", data);
        } catch (IOException e) {
            System.err.println("[FileController] 上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
