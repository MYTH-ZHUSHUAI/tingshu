package com.atguigu.tingshu.oss.oss.impl;

import com.atguigu.tingshu.oss.oss.OssService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "oss.provider", havingValue = "tencent")
public class TencentCosService implements OssService {

    @Override
    public String upload(MultipartFile file, String objectName) {
        throw new UnsupportedOperationException("Tencent COS not implemented");
    }

    @Override
    public InputStream download(String objectName) {
        throw new UnsupportedOperationException("Tencent COS not implemented");
    }
}
