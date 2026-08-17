const fs = require('fs');
const path = require('path');

// 設定 File Service URL (請依據實際 Port 調整，預設為 8081)
const BASE_URL = 'http://localhost:8081/api/v1/files/multipart';
const FILE_PATH = path.join(__dirname, 'test-large-file.bin');
const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB (S3/MinIO 分片最小限制為 5MB，最後一片除外)

async function testMultipartUpload() {
  console.log('1. 建立測試用的假檔案 (12MB)...');
  const totalSize = 12 * 1024 * 1024;
  const buffer = Buffer.alloc(totalSize, 'A'); // 建立全為 'A' 的 12MB 檔案
  fs.writeFileSync(FILE_PATH, buffer);

  try {
    console.log('\n2. 呼叫 Initiate Multipart Upload...');
    const initiateRes = await fetch(`${BASE_URL}/initiate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: 'DOCUMENT',
        originalFileName: 'test-large-file.bin',
        mimeType: 'application/octet-stream',
        size: totalSize,
        checksum: null,
        tags: null
      })
    });
    
    if (!initiateRes.ok) throw new Error(`Initiate failed: ${initiateRes.statusText}`);
    const initiateData = await initiateRes.json();
    const { fileId, uploadId } = initiateData;
    console.log(`✅ 取得 fileId: ${fileId}, uploadId: ${uploadId}`);

    console.log('\n3. 開始分片上傳 (Chunking & Presigned URLs)...');
    const fileBuffer = fs.readFileSync(FILE_PATH);
    const numParts = Math.ceil(fileBuffer.length / CHUNK_SIZE);
    const partETags = [];

    for (let i = 0; i < numParts; i++) {
      const partNumber = i + 1;
      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, fileBuffer.length);
      const chunk = fileBuffer.subarray(start, end);

      console.log(`   -> 取得 Part ${partNumber} 的 Presigned URL...`);
      const presignedRes = await fetch(`${BASE_URL}/${fileId}/${uploadId}/presigned-part?partNumber=${partNumber}`);
      if (!presignedRes.ok) throw new Error(`Failed to get presigned URL for part ${partNumber}`);
      const presignedUrl = await presignedRes.text();

      console.log(`   -> 上傳 Part ${partNumber} (${chunk.length} bytes)...`);
      const uploadRes = await fetch(presignedUrl, {
        method: 'PUT',
        body: chunk,
        headers: {
            // MinIO 有時會嚴格校驗 Content-Type
            'Content-Type': 'application/octet-stream'
        }
      });
      
      if (!uploadRes.ok) throw new Error(`Upload part ${partNumber} failed`);
      
      // 取出 ETag (MinIO 會回傳加上引號的 ETag)
      const eTag = uploadRes.headers.get('ETag');
      console.log(`      ✅ Part ${partNumber} 上傳成功, ETag: ${eTag}`);
      
      partETags.push({
        partNumber: partNumber,
        eTag: eTag
      });
    }

    console.log('\n4. 呼叫 Complete Multipart Upload...');
    const completeRes = await fetch(`${BASE_URL}/${fileId}/${uploadId}/complete`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        partETags: partETags
      })
    });

    if (!completeRes.ok) throw new Error(`Complete failed: ${await completeRes.text()}`);
    const completeData = await completeRes.json();
    console.log('🎉 分片上傳大功告成！', completeData);

  } catch (error) {
    console.error('\n❌ 測試失敗:', error);
  } finally {
    // 清理測試檔案
    if (fs.existsSync(FILE_PATH)) {
      fs.unlinkSync(FILE_PATH);
      console.log('\n🧹 測試檔案已清理。');
    }
  }
}

testMultipartUpload();
