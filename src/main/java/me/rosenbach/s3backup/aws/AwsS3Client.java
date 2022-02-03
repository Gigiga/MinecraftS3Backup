package me.rosenbach.s3backup.aws;

import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.transfer.s3.S3ClientConfiguration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.Upload;

import java.io.File;

public class AwsS3Client {
    private final S3TransferManager transferManager;
    private final long partSize = 10;

    public AwsS3Client(double uploadSpeed) {
        S3ClientConfiguration s3ClientConfiguration =
                S3ClientConfiguration.builder()
                        .minimumPartSizeInBytes(partSize * 1024 * 1024)
                        .targetThroughputInGbps(uploadSpeed)
                        .build();

        transferManager = S3TransferManager.builder().s3ClientConfiguration(s3ClientConfiguration).build();
    }

    public AwsS3Client(String region, String accessKey, String accessKeySecret, double uploadSpeed) {
        S3ClientConfiguration s3ClientConfiguration =
                S3ClientConfiguration.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, accessKeySecret)))
                        .minimumPartSizeInBytes(partSize * 1024 * 1024)
                        .targetThroughputInGbps(uploadSpeed)
                        .build();

        transferManager = S3TransferManager.builder().s3ClientConfiguration(s3ClientConfiguration).build();
    }

    public void putObject(String bucket, String key, File file) {
        Upload upload =
                transferManager.upload(b -> b.putObjectRequest(r -> r.bucket(bucket).key(key))
                        .source(file.toPath()));

        upload.completionFuture().join();
    }

    public void close() {
        transferManager.close();
    }
}
