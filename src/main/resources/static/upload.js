const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB chunks

async function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];
    if (!file) {
        alert('Please select a file');
        return;
    }

    const progressBar = document.getElementById('progressBar');
    const status = document.getElementById('status');
    
    try {
        // 1. Initiate multipart upload
        const initiateResponse = await fetch('/api/upload/initiate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                key: file.name
            })
        });
        
        const { uploadId, key, bucket, region } = await initiateResponse.json();
        
        // 2. Upload parts
        const chunks = Math.ceil(file.size / CHUNK_SIZE);
        const uploadedParts = [];
        
        for (let i = 0; i < chunks; i++) {
            const start = i * CHUNK_SIZE;
            const end = Math.min(start + CHUNK_SIZE, file.size);
            const chunk = file.slice(start, end);
            
            // Get new signature for each part
            const partSignatureResponse = await fetch('/api/upload/part-signature', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    key: key,
                    uploadId: uploadId,
                    partNumber: i + 1
                })
            });
            
            const partSignatureData = await partSignatureResponse.json();
            
            const encodedKey = encodeURIComponent(key);
            const url = new URL(`https://${bucket}.s3.${region}.amazonaws.com/${encodedKey}`);
            url.searchParams.append('partNumber', (i + 1).toString());
            url.searchParams.append('uploadId', uploadId);

            const response = await fetch(url.toString(), {
                method: 'PUT',
                headers: {
                    'Authorization': partSignatureData.authorizationHeader,
                    'x-amz-date': partSignatureData.amzDate,
                    'x-amz-content-sha256': 'UNSIGNED-PAYLOAD'
                },
                body: chunk
            });
            
            if (response.ok) {
                const eTag = response.headers.get('ETag');
                uploadedParts.push({
                    partNumber: i + 1,
                    eTag: eTag.replace(/"/g, '')
                });
                
                // Update progress
                const progress = ((i + 1) / chunks) * 100;
                progressBar.style.width = `${progress}%`;
                status.textContent = `Uploading: ${Math.round(progress)}%`;
            } else {
                throw new Error(`Failed to upload part ${i + 1}`);
            }
        }
        
        // 3. Complete multipart upload
        const completeResponse = await fetch('/api/upload/complete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                key: key,
                uploadId: uploadId,
                parts: uploadedParts
            })
        });

        if (!completeResponse.ok) {
            throw new Error('Failed to complete multipart upload');
        }

        status.textContent = 'Upload completed successfully!';
        progressBar.style.width = '100%';
        
    } catch (error) {
        console.error('Upload failed:', error);
        status.textContent = `Upload failed: ${error.message}`;
        progressBar.style.backgroundColor = '#ff0000';
    }
} 