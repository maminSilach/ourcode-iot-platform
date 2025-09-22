package com.example.failedeventsprocessor.service;

import com.example.failedeventsprocessor.dto.response.FileResponse;
import com.example.failedeventsprocessor.exception.AwsException;
import com.example.failedeventsprocessor.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${aws.host}")
    private final String host;

    @Value("${aws.region-static}")
    private final String region;

    private final S3Client s3Client;

    private final FileMapper fileMapper;

    @Retryable(
            retryFor = AwsException.class,
            maxAttemptsExpression = "${aws.retry.attempts}",
            backoff = @Backoff(delayExpression = "${aws.retry.backoff}")
    )
    public FileResponse uploadJsonResourceWithRetry(String resource, String key, String bucketName) {
        byte[] resourceBytes = resource.getBytes();

        try (InputStream inputStream = new ByteArrayInputStream(resourceBytes)) {

            log.info("Start put file in S3 with next key = {} to bucket = {}", key, bucketName);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            putObject(putObjectRequest, inputStream, resourceBytes);
            String url = getUrl(key, bucketName);

            return fileMapper.toFileResponse(url, key);
        } catch (IOException e) {
            throw new AwsException(e);
        }
    }

    @Recover
    public void recoverUploadJsonResource(AwsException exception, String resource, String key, String bucketName) {
        log.error("Failed to upload file to S3 after retries - bucket: {}, key: {}, error: {}",
                bucketName, key, exception.getMessage());
    }

    public ListObjectsResponse loadFilesByPrefix(String prefix, String bucketName) {
        log.info("Loading file from bucket = {} with prefix = {}", bucketName, prefix);

        return s3Client.listObjects(
                ListObjectsRequest.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build()
        );
    }

    public byte[] loadFile(String key, String bucketName) {
        log.info("Loading file from bucket = {} with key = {}", bucketName, key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return getObject(getObjectRequest);
    }

    private String getUrl(String key, String bucketName) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucketName)
                        .region(Region.of(region))
                        .endpoint(URI.create(host))
                        .key(key)
                        .build()
                ).toExternalForm();
    }

    private void putObject(PutObjectRequest putObjectRequest, InputStream inputStream, byte[] bytes) {
        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, bytes.length));
        } catch (RuntimeException e) {
            log.error("Error while trying to put file in S3", e);
            throw new AwsException(e);
        }
    }

    private byte[] getObject(GetObjectRequest getObjectRequest) {
        try {
            return s3Client.getObject(getObjectRequest).readAllBytes();
        } catch (IOException | RuntimeException e) {
            log.error("Error while trying to load file from S3", e);
            throw new AwsException(e);
        }
    }
}