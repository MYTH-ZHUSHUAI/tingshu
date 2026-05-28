package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.TrackFileMapper;
import com.atguigu.tingshu.album.service.TrackFileService;
import com.atguigu.tingshu.model.file.TrackFile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TrackFileServiceImpl extends ServiceImpl<TrackFileMapper, TrackFile> implements TrackFileService {
}
