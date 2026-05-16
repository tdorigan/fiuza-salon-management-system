package com.nci.fiuza.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2StorageConfig {

    //reads the Cloudflare R2 endpoint from application.properties / environment variables
    @Value("${cloudflare.r2.endpoint:}")
    private String r2Endpoint;

    //reads the Cloudflare R2 access key id from application.properties / environment variables
    @Value("${cloudflare.r2.access-key-id:}")
    private String r2AccessKeyId;

    //reads the Cloudflare R2 secret access key from application.properties / environment variables
    @Value("${cloudflare.r2.secret-access-key:}")
    private String r2SecretAccessKey;

    //creates one reusable S3Client bean for Cloudflare R2
    @Bean
    public S3Client r2S3Client() {

        //creates AWS-style credentials using Cloudflare R2 keys
        AwsBasicCredentials credentials = AwsBasicCredentials.create(r2AccessKeyId, r2SecretAccessKey);

        //builds and returns the S3-compatible client configured for Cloudflare R2
        return S3Client.builder()
                .endpointOverride(URI.create(r2Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .build();
    }
}
