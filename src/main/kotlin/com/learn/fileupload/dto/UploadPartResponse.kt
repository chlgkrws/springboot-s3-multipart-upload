package com.learn.fileupload.dto

data class UploadPartResponse(
    val uploadId: String,
    val key: String,
    val bucket: String,
    val region: String
) 