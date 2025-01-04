# S3 Multipart Upload Example

A demonstration of S3 Multipart Upload implementation using AWS SDK.

## Tech Stack

- Kotlin
- Spring Boot
- AWS SDK v2
- AWS S3

## Features

- Large file multipart upload
- Custom AWS Signature V4 implementation
- Progress tracking
- Chunk-based upload

## Configuration

Add the following configuration to `application.yml`:

```yaml
aws:
  s3:
    bucket-name: your-bucket-name
    region: ap-northeast-2
    access-key: your-access-key
    secret-key: your-secret-key
```

## API Endpoints

### 1. Initialize Multipart Upload
```http
POST /api/upload/initiate
Content-Type: application/json

{
    "key": "filename.ext"
}
```

### 2. Generate Part Upload Signature
```http
POST /api/upload/part-signature
Content-Type: application/json

{
    "key": "filename.ext",
    "uploadId": "upload-id",
    "partNumber": 1
}
```

### 3. Complete Multipart Upload
```http
POST /api/upload/complete
Content-Type: application/json

{
    "key": "filename.ext",
    "uploadId": "upload-id",
    "parts": [
        {
            "partNumber": 1,
            "eTag": "etag-value"
        }
    ]
}
```

## Getting Started

1. Clone the project
2. Add AWS configuration to `application.yml`
3. Run the application
4. Access `http://localhost:8080` in your browser
5. Select a file and click the upload button

## Project Structure

```
src/main/kotlin/com/learn/fileupload/
├── config/
│   └── S3Properties.kt
├── controller/
│   └── S3UploadController.kt
├── dto/
│   ├── CompleteMultipartUploadRequest.kt
│   ├── CompleteUploadResponse.kt
│   ├── InitiateMultipartUploadRequest.kt
│   ├── UploadPartResponse.kt
│   └── UploadPartSignatureRequest.kt
├── service/
│   └── S3UploadService.kt
└── util/
    └── AwsSignatureV4.kt

src/main/resources/static/
├── index.html
└── upload.js
```

## Implementation Details

- Custom implementation of AWS Signature V4 for S3 authentication
- Files are split into xMB chunks and uploaded sequentially
- New signature is generated for each part upload
- Multipart upload is completed through S3 API after all parts are uploaded
- Frontend progress tracking for upload status

## Key Components

- **AwsSignatureV4**: Handles AWS Signature V4 signing process
- **S3UploadService**: Manages the multipart upload workflow
- **S3UploadController**: Provides REST endpoints for the upload process
- **upload.js**: Handles client-side file chunking and upload process

## Notes
- AWS credentials should be properly secured
