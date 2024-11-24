package com.learn.fileupload.dto

data class UploadPartSignatureRequest(
    val key: String,
    val uploadId: String,
    val partNumber: Int
) 