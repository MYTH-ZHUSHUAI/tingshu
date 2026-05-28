package com.atguigu.tingshu.oss.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.file.FileMetadata;
import com.atguigu.tingshu.oss.oss.OssService;
import com.atguigu.tingshu.oss.service.FileMetadataService;
import com.atguigu.tingshu.vo.oss.FileMetadataVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/oss")
public class OssApiController {

    @Resource
    private OssService ossService;
    @Resource
    private FileMetadataService fileMetadataService;

    @PostMapping("/upload")
    public Result<FileMetadataVo> upload(@RequestPart MultipartFile file) {
        try {
            String hash = DigestUtils.md5DigestAsHex(file.getInputStream()).toUpperCase();
            FileMetadata existing = fileMetadataService.findByHash(hash);
            if (existing != null) {
                return Result.ok(toVo(existing));
            }

            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String objectName = UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
            String url = ossService.upload(file, objectName);

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileKey(objectName);
            metadata.setFileUrl(url);
            metadata.setFileSize(file.getSize());
            metadata.setFileHash(hash);
            metadata.setStorageType("minio");
            fileMetadataService.save(metadata);

            return Result.ok(toVo(metadata));

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.fail();
        }
    }

    @GetMapping("/download/{fileKey}")
    public void download(@PathVariable String fileKey, HttpServletResponse response) {
        try (InputStream in = ossService.download(fileKey)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileKey + "\"");
            IOUtils.copy(in, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileKey, e);
            response.setStatus(500);
        }
    }

    private FileMetadataVo toVo(FileMetadata metadata) {
        FileMetadataVo vo = new FileMetadataVo();
        vo.setId(metadata.getId());
        vo.setFileName(metadata.getFileName());
        vo.setFileKey(metadata.getFileKey());
        vo.setFileUrl(metadata.getFileUrl());
        vo.setFileSize(metadata.getFileSize());
        return vo;
    }
}
