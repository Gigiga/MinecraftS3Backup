package me.rosenbach.s3backup.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;

public class AwsS3Client {
    private final S3Client s3;

    public AwsS3Client(String region) {
        s3 = S3Client.builder().region(Region.of(region)).build();
    }

    public AwsS3Client(String region, String accessKey, String accessKeySecret) {
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, accessKeySecret);
        s3 = S3Client.builder().region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
    }

    public void putObject(String bucket, File file) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(file.getName())
                .build();

        s3.putObject(objectRequest, RequestBody.fromFile(file));
    }
}
