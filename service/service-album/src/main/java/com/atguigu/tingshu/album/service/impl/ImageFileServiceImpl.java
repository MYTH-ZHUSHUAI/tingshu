package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.ImageFileMapper;
import com.atguigu.tingshu.album.service.ImageFileService;
import com.atguigu.tingshu.model.file.ImageFile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ImageFileServiceImpl extends ServiceImpl<ImageFileMapper, ImageFile> implements ImageFileService {
}
