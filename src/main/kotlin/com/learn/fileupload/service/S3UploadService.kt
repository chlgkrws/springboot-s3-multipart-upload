package com.learn.fileupload.service

import com.learn.fileupload.config.S3Properties
import com.learn.fileupload.dto.CompleteUploadRequest
import com.learn.fileupload.dto.InitiateMultipartUploadRequest
import com.learn.fileupload.dto.UploadPartResponse
import com.learn.fileupload.dto.UploadPartSignatureRequest
import com.learn.fileupload.util.AwsSignatureV4
import com.learn.fileupload.util.SignatureData
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload
import software.amazon.awssdk.services.s3.model.CompletedPart
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class S3UploadService(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties,
    private val awsSignatureV4: AwsSignatureV4
) {
    suspend fun initiateMultipartUpload(request: InitiateMultipartUploadRequest): UploadPartResponse {
        val createRequest = CreateMultipartUploadRequest.builder()
            .bucket(s3Properties.bucketName)
            .key(request.key)
            .build()

        val response = s3Client.createMultipartUpload(createRequest)

        return UploadPartResponse(
            uploadId = response.uploadId(),
            key = request.key,
            bucket = s3Properties.bucketName,
            region = s3Properties.region
        )
    }

    suspend fun getUploadPartSignature(request: UploadPartSignatureRequest): SignatureData {
        val dateTime = LocalDateTime.now(ZoneOffset.UTC)
        val encodedKey = URLEncoder.encode(request.key, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
        
        val host = "${s3Properties.bucketName}.s3.${s3Properties.region}.amazonaws.com"
        
        val headers = linkedMapOf(
            "host" to host,
            "x-amz-content-sha256" to "UNSIGNED-PAYLOAD",
            "x-amz-date" to dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
        )

        val queryParams = linkedMapOf(
            "partNumber" to request.partNumber.toString(),
            "uploadId" to request.uploadId
        )

        return awsSignatureV4.generateSignature(
            httpMethod = "PUT",
            canonicalUri = "/$encodedKey",
            queryParams = queryParams,
            headers = headers,
            payload = "",
            dateTime = dateTime
        )
    }

    suspend fun completeMultipartUpload(request: CompleteUploadRequest) {
        val completedParts = request.parts.map { part ->
            CompletedPart.builder()
                .partNumber(part.partNumber)
                .eTag(part.eTag)
                .build()
        }

        val completedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(completedParts)
            .build()

        val completeRequest = CompleteMultipartUploadRequest.builder()
            .bucket(s3Properties.bucketName)
            .key(request.key)
            .uploadId(request.uploadId)
            .multipartUpload(completedMultipartUpload)
            .build()

        s3Client.completeMultipartUpload(completeRequest)
    }
} 