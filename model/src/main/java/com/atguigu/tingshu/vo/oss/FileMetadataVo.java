package com.atguigu.tingshu.vo.oss;

import lombok.Data;

@Data
public class FileMetadataVo {

    private Long id;
    private String fileName;
    private String fileKey;
    private String fileUrl;
    private Long fileSize;
}
