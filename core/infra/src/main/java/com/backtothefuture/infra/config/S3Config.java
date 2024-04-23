package com.backtothefuture.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration

public class S3Config {

    @Value("${cloud.aws.s3.region}")
    String regionString;

    @Value("${cloud.aws.s3.accessKey}")
    String accessKey;

    @Value("${cloud.aws.s3.secretKey}")
    String secretKey;

    @Bean
    public AwsCredentials basicAWSCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Client defaultS3Client(AwsCredentials awsCredentials) {
        return S3Client.builder()
                .region(Region.of(regionString))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
