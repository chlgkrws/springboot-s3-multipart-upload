package com.learn.fileupload.dto

data class CompleteUploadRequest(
    val key: String,
    val uploadId: String,
    val parts: List<CompletedPartInfo>
)

data class CompletedPartInfo(
    val partNumber: Int,
    val eTag: String
) 