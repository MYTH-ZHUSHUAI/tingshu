package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.ImageFileService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.file.ImageFile;
import com.atguigu.tingshu.oss.client.FileFeignClient;
import com.atguigu.tingshu.vo.oss.FileMetadataVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "图片上传")
@RestController
@RequestMapping("api/album")
public class FileuploadController {

    @Resource
    private FileFeignClient fileFeignClient;
    @Resource
    private ImageFileService imageFileService;

    @PostMapping("fileUpload")
    public Result<String> fileupload(MultipartFile file) {

        if (file == null) {
            throw new GuiguException(ResultCodeEnum.FAIL);
        }

        FileMetadataVo vo = fileFeignClient.upload(file).getData();

        // 已有相同URL的记录则不重复创建
        ImageFile existImageFile = imageFileService.getOne(
                new LambdaQueryWrapper<ImageFile>().eq(ImageFile::getFileUrl, vo.getFileUrl()));
        if (existImageFile == null) {
            ImageFile imageFile = new ImageFile();
            imageFile.setFileName(vo.getFileName());
            imageFile.setFileUrl(vo.getFileUrl());
            imageFile.setFileSize(vo.getFileSize());
            imageFile.setStorageType("minio");
            imageFile.setUploadUserId(AuthContextHolder.getUserId());
            imageFile.setRefCount(0);
            imageFile.setStatus(0);
            imageFileService.save(imageFile);
        }

        return Result.ok(vo.getFileUrl());
    }
}
