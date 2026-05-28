package com.atguigu.tingshu.oss.oss;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface OssService {

    String upload(MultipartFile file, String objectName);

    InputStream download(String objectName);
}
