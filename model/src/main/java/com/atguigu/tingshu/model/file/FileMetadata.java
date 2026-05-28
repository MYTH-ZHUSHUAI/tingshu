package com.atguigu.tingshu.model.file;

import com.atguigu.tingshu.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

@Data
@Schema(description = "文件元数据")
@TableName("file_metadata")
public class FileMetadata extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "存储路径/对象名")
    @TableField("file_key")
    private String fileKey;

    @Schema(description = "完整访问URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "文件大小(字节)")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "文件MD5哈希值")
    @TableField("file_hash")
    private String fileHash;

    @Schema(description = "存储类型: minio/vod")
    @TableField("storage_type")
    private String storageType;

    @Schema(description = "存储桶名称")
    @TableField("bucket_name")
    private String bucketName;

    @Schema(description = "引用计数")
    @TableField("ref_count")
    private Integer refCount;

    @Schema(description = "状态: 0-临时 1-已绑定 2-已废弃")
    @TableField("status")
    private Integer status;

    @Schema(description = "上传用户ID")
    @TableField("upload_user_id")
    private Long uploadUserId;
}
