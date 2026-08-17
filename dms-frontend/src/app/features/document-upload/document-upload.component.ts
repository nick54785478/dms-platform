import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { DocumentService } from '../../services/document.service';
import { lastValueFrom } from 'rxjs';

/**
 * 文件上傳元件 (Document Upload Component)
 * 負責提供使用者上傳新文件檔案的表單介面，並處理「小檔案一般上傳」與「大檔案分段上傳 (Multipart Upload)」的複雜邏輯。
 * 上傳完成後，將觸發業務系統的文件綁定流程。
 */
@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    InputTextareaModule,
    ButtonModule,
    FileUploadModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './document-upload.component.html',
  styleUrl: './document-upload.component.css'
})
export class DocumentUploadComponent {
  /** 上傳表單，包含標題與描述等業務中繼資料 */
  uploadForm: FormGroup;
  /** 使用者所選取準備上傳的實體檔案物件 */
  selectedFile: File | null = null;
  /** 控制畫面上的上傳進度與鎖定狀態 (防止重複提交) */
  uploading = false;

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService,
    private messageService: MessageService
  ) {
    this.uploadForm = this.fb.group({
      title: ['', Validators.required],
      description: ['']
    });
  }

  /**
   * 當使用者在 FileUpload 元件選擇檔案時觸發，暫存檔案參考
   * @param event PrimeNG FileUpload 事件
   */
  onFileSelect(event: any) {
    // PrimeNG fileUpload selection
    if (event.files && event.files.length > 0) {
      this.selectedFile = event.files[0];
    }
  }

  /**
   * 當使用者在 FileUpload 元件移除已選檔案時觸發，清除暫存
   * @param event PrimeNG FileUpload 事件
   */
  onFileRemove(event: any) {
    this.selectedFile = null;
  }

  /**
   * 執行文件上傳的完整非同步流程。
   * 依據檔案大小自動切換「直接 Pre-signed URL 上傳」(< 5MB) 或「Multipart Upload 分段上傳」(>= 5MB)。
   * 上傳完畢後，向後端建立 Document 並進行 File Binding。
   * 
   * @param fileUploader 畫面上 PrimeNG 的 FileUpload 元件實體 (用於上傳成功後清空畫面)
   */
  async submitDocument(fileUploader: any) {
    if (this.uploadForm.invalid) {
      this.messageService.add({ severity: 'warn', summary: '警告', detail: '請填寫必填欄位 (標題)' });
      return;
    }

    if (!this.selectedFile) {
      this.messageService.add({ severity: 'warn', summary: '警告', detail: '請選擇要上傳的檔案' });
      return;
    }

    this.uploading = true;
    const { title, description } = this.uploadForm.value;
    const fileToUpload = this.selectedFile;
    const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB

    try {
      let fileId: string;

      if (fileToUpload.size <= CHUNK_SIZE) {
        // 小檔案流程：使用一般 Pre-signed URL 上傳
        const presignedResp = await lastValueFrom(this.documentService.getPresignedUploadUrl(fileToUpload.name, fileToUpload.type, fileToUpload.size));
        if (!presignedResp || !presignedResp.url || !presignedResp.fileId) {
          throw new Error('無法取得上傳憑證');
        }
        await lastValueFrom(this.documentService.uploadToStorage(presignedResp.url, fileToUpload));
        fileId = presignedResp.fileId;
      } else {
        // 大檔案流程：使用 Multipart Upload 分段上傳
        const initResp = await lastValueFrom(this.documentService.initiateMultipartUpload(fileToUpload.name, fileToUpload.type, fileToUpload.size));
        if (!initResp || !initResp.uploadId || !initResp.fileId) {
          throw new Error('無法初始化分段上傳');
        }

        const partETags: Record<number, string> = {};
        const numParts = Math.ceil(fileToUpload.size / CHUNK_SIZE);

        for (let i = 0; i < numParts; i++) {
          const partNumber = i + 1;
          const start = i * CHUNK_SIZE;
          const end = Math.min(start + CHUNK_SIZE, fileToUpload.size);
          const chunk = fileToUpload.slice(start, end);

          // 取得分段上傳網址
          const partUrl = await lastValueFrom(this.documentService.getPresignedPartUrl(initResp.fileId, initResp.uploadId, partNumber));

          // 上傳該分段
          const uploadResp = await lastValueFrom(this.documentService.uploadToStorage(partUrl, chunk));

          // 擷取 ETag (注意：S3 必須開放 ExposeHeaders: ["ETag"])
          const eTag = uploadResp.headers.get('ETag');
          if (!eTag) {
            console.warn(`第 ${partNumber} 段的 ETag 遺失，若 S3/MinIO 未設定 ExposeHeaders 可能會導致合併失敗`);
          }
          // 去除 ETag 可能自帶的雙引號
          partETags[partNumber] = eTag ? eTag.replace(/"/g, '') : 'dummy-etag';
        }

        // 呼叫伺服器完成合併
        await lastValueFrom(this.documentService.completeMultipartUpload(initResp.fileId, initResp.uploadId, partETags));
        fileId = initResp.fileId;
      }

      // 最後一步：業務綁定
      const docResp = await lastValueFrom(this.documentService.createDocument(title, description, fileId));

      this.uploading = false;
      this.messageService.add({ severity: 'success', summary: '成功', detail: `文件建立成功: ${docResp.title}` });
      this.uploadForm.reset();
      this.selectedFile = null;
      fileUploader.clear(); // 清空 PrimeNG 檔案上傳元件

    } catch (err: any) {
      this.uploading = false;
      console.error(err);

      // 提取具體錯誤訊息
      let errMsg = '未知錯誤';
      if (err.error instanceof ErrorEvent) {
        errMsg = err.error.message;
      } else if (err.error && err.error.message) {
        errMsg = err.error.message;
      } else if (err.status === 0) {
        errMsg = '無法連線至伺服器或發生跨域 (CORS) 問題';
      } else if (err.message) {
        errMsg = err.message;
      }

      this.messageService.add({ severity: 'error', summary: '上傳失敗', detail: errMsg });
    }
  }
}
