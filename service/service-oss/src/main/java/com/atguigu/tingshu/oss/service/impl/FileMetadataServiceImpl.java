package com.atguigu.tingshu.oss.service.impl;

import com.atguigu.tingshu.model.file.FileMetadata;
import com.atguigu.tingshu.oss.mapper.FileMetadataMapper;
import com.atguigu.tingshu.oss.service.FileMetadataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataServiceImpl
        extends ServiceImpl<FileMetadataMapper, FileMetadata>
        implements FileMetadataService {

    @Override
    public FileMetadata findByHash(String fileHash) {
        return lambdaQuery().eq(FileMetadata::getFileHash, fileHash).one();
    }
}
