/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.config.S3Config
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
 *  software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
 *  software.amazon.awssdk.regions.Region
 *  software.amazon.awssdk.services.s3.S3Client
 *  software.amazon.awssdk.services.s3.S3ClientBuilder
 */
package com.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@Profile(value={"prod"})
public class S3Config {
    @Value(value="${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return (S3Client)((S3ClientBuilder)((S3ClientBuilder)S3Client.builder().region(Region.of((String)this.region))).credentialsProvider((AwsCredentialsProvider)DefaultCredentialsProvider.create())).build();
    }
}

