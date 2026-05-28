package com.atguigu.tingshu.oss.client.impl;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.oss.client.FileFeignClient;
import com.atguigu.tingshu.vo.oss.FileMetadataVo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileDegradeFeignClient implements FileFeignClient {

    @Override
    public Result<FileMetadataVo> upload(MultipartFile file) {
        return Result.fail();
    }
}
