package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.TrackFileService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.file.TrackFile;
import com.atguigu.tingshu.oss.client.FileFeignClient;
import com.atguigu.tingshu.vo.oss.FileMetadataVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class VodServiceImpl implements VodService {

    @Resource
    private VodConstantProperties vodConstantProperties;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private TrackFileService trackFileService;

    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
        Path tempDir = Paths.get(vodConstantProperties.getTempPath());
        Path localPath = tempDir.resolve(fileName);

        String fileHash;
        try {
            Files.createDirectories(tempDir);

            // 边写临时文件边计算 MD5，一次 I/O
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream in = file.getInputStream();
                 OutputStream out = Files.newOutputStream(localPath)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    md.update(buffer, 0, n);
                    out.write(buffer, 0, n);
                }
            }
            fileHash = HexFormat.of().formatHex(md.digest());
            log.info("文件暂存到本地: {}, md5={}", localPath, fileHash);
        } catch (IOException e) {
            log.error("本地文件暂存失败", e);
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5算法不可用", e);
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        }

        try {
            // 秒传：同MD5已存在则跳过上传
            TrackFile existFile = trackFileService.getOne(
                    new LambdaQueryWrapper<TrackFile>().eq(TrackFile::getFileHash, fileHash));
            if (existFile != null) {
                log.info("秒传命中, fileHash={}, mediaFileId={}", fileHash, existFile.getMediaFileId());
                Map<String, Object> map = new HashMap<>();
                map.put("mediaFileId", existFile.getMediaFileId());
                map.put("mediaUrl", existFile.getMediaUrl());
                map.put("fileUrl", existFile.getFileUrl());
                return map;
            }

            // 上传到 MinIO
            MultipartFile minioFile = wrapMultipartFile(localPath, fileName);
            FileMetadataVo minioResult = fileFeignClient.upload(minioFile).getData();
            String fileUrl = minioResult.getFileUrl();
            log.info("MinIO上传成功: {}", fileUrl);

            // 上传到腾讯云VOD
            VodUploadClient client =
                    new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
            VodUploadRequest request = new VodUploadRequest();
            request.setMediaFilePath(localPath.toString());
            VodUploadResponse response = client.upload(vodConstantProperties.getRegion(), request);
            log.info("Upload RequestId = {}", response.getRequestId());

            // 写入业务文件表
            TrackFile trackFile = new TrackFile();
            trackFile.setFileName(file.getOriginalFilename());
            trackFile.setMediaFileId(response.getFileId());
            trackFile.setMediaUrl(response.getMediaUrl());
            trackFile.setFileUrl(fileUrl);
            trackFile.setFileSize(file.getSize());
            trackFile.setFileHash(fileHash);
            trackFile.setUploadUserId(AuthContextHolder.getUserId());
            trackFile.setRefCount(0);
            trackFile.setStatus(0);
            trackFileService.save(trackFile);

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

    /**
     * 将本地临时文件包装为 MultipartFile，用于 Feign 调用 MinIO 上传
     */
    private MultipartFile wrapMultipartFile(Path filePath, String fileName) {
        return new MultipartFile() {
            @NotNull
            @Override
            public String getName() { return "file"; }

            @Override
            public String getOriginalFilename() { return fileName; }

            @Override
            public String getContentType() { return "audio/mpeg"; }

            @Override
            public boolean isEmpty() { return false; }

            @Override
            public long getSize() {
                try { return Files.size(filePath); }
                catch (IOException e) { return 0; }
            }

            @Override
            public byte[] getBytes() throws IOException { return Files.readAllBytes(filePath); }

            @Override
            public InputStream getInputStream() throws IOException { return Files.newInputStream(filePath); }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(filePath, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        };
    }
}
