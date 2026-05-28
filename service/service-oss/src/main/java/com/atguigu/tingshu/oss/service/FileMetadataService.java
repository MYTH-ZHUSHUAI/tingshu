package com.atguigu.tingshu.oss.service;

import com.atguigu.tingshu.model.file.FileMetadata;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FileMetadataService extends IService<FileMetadata> {

    FileMetadata findByHash(String fileHash);
}
