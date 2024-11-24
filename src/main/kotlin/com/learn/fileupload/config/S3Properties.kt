package com.learn.fileupload.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
	var bucketName: String,
	var region: String,
	var accessKey: String,
	var secretKey: String
)
