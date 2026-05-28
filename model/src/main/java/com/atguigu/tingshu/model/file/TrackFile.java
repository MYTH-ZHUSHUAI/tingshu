package com.atguigu.tingshu.model.file;

import com.atguigu.tingshu.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@Schema(description = "声音业务文件")
@TableName("track_file")
public class TrackFile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "VOD媒体文件ID")
    @TableField("media_file_id")
    private String mediaFileId;

    @Schema(description = "VOD播放地址")
    @TableField("media_url")
    private String mediaUrl;

    @Schema(description = "文件大小(字节)")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "音频时长(秒)")
    @TableField("media_duration")
    private BigDecimal mediaDuration;

    @Schema(description = "上传用户ID")
    @TableField("upload_user_id")
    private Long uploadUserId;

    @Schema(description = "引用计数")
    @TableField("ref_count")
    private Integer refCount;

    @Schema(description = "状态: 0-临时 1-已绑定 2-已废弃")
    @TableField("status")
    private Integer status;

    @Schema(description = "文件MD5哈希")
    @TableField("file_hash")
    private String fileHash;

    @Schema(description = "MinIO文件URL")
    @TableField("file_url")
    private String fileUrl;
}
