package com.petadopt.controller;

import com.petadopt.common.BusinessException;
import com.petadopt.common.Result;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传 / 下载接口
 * <p>
 * 负责图片等文件的「上传」和「下载」。上传后返回一个可访问的下载地址，
 * 供宠物照片、轮播图、文章封面等场景使用。文件保存在项目目录下的 uploads 文件夹中。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    // 上传文件的存放目录：项目根目录下的 uploads 文件夹
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    /**
     * 上传文件（如图片）
     * <p>
     * 请求方式：POST /api/file/upload
     * 入参：file（表单字段名为 "file" 的 MultipartFile 文件）
     * 内部步骤：
     *   1. 校验文件是否为空，为空则抛出业务异常「请选择文件」；
     *   2. 确保 uploads 目录存在，不存在则创建；
     *   3. 用「时间戳_原文件名」生成唯一文件名，避免重名覆盖；
     *   4. 保存文件到磁盘；
     *   5. 返回可访问的下载地址。
     * 返回：Result&lt;Map&lt;String,String&gt;&gt;，data 中的 "url" 键为下载地址
     * 权限：无（登录用户即可）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + original;
            file.transferTo(new File(dir, filename).getAbsoluteFile());
            Map<String, String> map = new HashMap<>();
            map.put("url", "/api/file/download/" + filename);
            return Result.success(map);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 下载文件（按文件名）
     * <p>
     * 请求方式：GET /api/file/download/{filename}
     * 入参：filename（路径参数，文件名，通常来自上传接口返回的 url）
     * 内部步骤：
     *   1. 根据文件名定位磁盘文件；
     *   2. 文件不存在则返回 404；
     *   3. 读取文件字节并作为二进制流返回给浏览器下载。
     * 返回：ResponseEntity&lt;byte[]&gt;，二进制文件内容；不存在时返回 404
     * 权限：无
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        try {
            File file = new File(UPLOAD_DIR + filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
