package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class VodServiceImpl implements VodService {

    @Resource
    private VodConstantProperties vodConstantProperties;

    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
        Path tempDir = Paths.get(vodConstantProperties.getTempPath());
        Path localPath = tempDir.resolve(fileName);

        try {
            Files.createDirectories(tempDir);
            file.transferTo(localPath.toFile());
            log.info("文件暂存到本地: {}", localPath);
        } catch (IOException e) {
            log.error("本地文件暂存失败", e);
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        }

        try {
            VodUploadClient client =
                    new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());

            VodUploadRequest request = new VodUploadRequest();
            request.setMediaFilePath(localPath.toString());

            VodUploadResponse response = client.upload(vodConstantProperties.getRegion(), request);
            log.info("Upload RequestId = {}", response.getRequestId());

            Map<String, Object> map = new HashMap<>();
            map.put("mediaFileId", response.getFileId());
            map.put("mediaUrl", response.getMediaUrl());

            return map;

        } catch (Exception e) {
            log.error("上传腾讯云VOD失败", e);
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        } finally {
            try {
                Files.deleteIfExists(localPath);
                log.info("删除本地暂存文件: {}", localPath);
            } catch (IOException e) {
                log.warn("删除本地暂存文件失败: {}", localPath, e);
            }
        }
    }
}
