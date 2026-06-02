package com.atguigu.tingshu.oss.oss.impl;

import com.atguigu.tingshu.oss.config.OssProperties;
import com.atguigu.tingshu.oss.oss.OssService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "oss.provider", havingValue = "minio", matchIfMissing = true)
public class MinioOssService implements OssService {

    @Resource
    private MinioClient minioClient;
    @Resource
    private OssProperties ossProperties;

    @Override
    public String upload(MultipartFile file, String objectName) {
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(ossProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return ossProperties.getMinio().getEndpointUrl()
                    + "/" + ossProperties.getMinio().getBucketName()
                    + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    @Override
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(ossProperties.getMinio().getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO download failed", e);
        }
    }

    private void ensureBucket() {
        try {
            String bucketName = ossProperties.getMinio().getBucketName();
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
            if (ossProperties.getMinio().isPublicRead()) {
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": ["*"]},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO bucket check failed", e);
        }
    }
}
