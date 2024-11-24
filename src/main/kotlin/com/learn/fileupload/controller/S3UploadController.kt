package com.learn.fileupload.controller

import com.learn.fileupload.dto.CompleteUploadRequest
import com.learn.fileupload.dto.InitiateMultipartUploadRequest
import com.learn.fileupload.dto.UploadPartResponse
import com.learn.fileupload.dto.UploadPartSignatureRequest
import com.learn.fileupload.service.S3UploadService
import com.learn.fileupload.util.SignatureData
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/upload")
class S3UploadController(
    private val s3UploadService: S3UploadService
) {

    @PostMapping("/initiate")
    suspend fun initiateMultipartUpload(
        @RequestBody request: InitiateMultipartUploadRequest
    ): UploadPartResponse {
        return s3UploadService.initiateMultipartUpload(request)
    }

    @PostMapping("/part-signature")
    suspend fun getUploadPartSignature(
        @RequestBody request: UploadPartSignatureRequest
    ): SignatureData {
        return s3UploadService.getUploadPartSignature(request)
    }

    @PostMapping("/complete")
    suspend fun completeMultipartUpload(
        @RequestBody request: CompleteUploadRequest
    ) {
        return s3UploadService.completeMultipartUpload(request)
    }
}