package com.atguigu.tingshu.oss.client;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.oss.client.impl.FileDegradeFeignClient;
import com.atguigu.tingshu.vo.oss.FileMetadataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "service-oss", fallback = FileDegradeFeignClient.class)
public interface FileFeignClient {

    @PostMapping(value = "/api/oss/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<FileMetadataVo> upload(@RequestPart("file") MultipartFile file);
}
