import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileResponse, DocumentCreatedResource, PresignedUrlResponse, InitiateMultipartResult, PageRetrievedResource, DocumentSearchedResource, DocumentRetrievedResource } from '../core/models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  // Assume file-service is on 8081 and dms-service is on 8080
  private readonly fileServiceUrl = 'http://localhost:8081/api/v1/files';
  private readonly dmsServiceUrl = 'http://localhost:8080/api/v1/documents';

  constructor(private http: HttpClient) { }

  /**
   * 取得單一文件詳細資訊與歷史版本
   * @param id 文件 ID
   */
  getDocument(id: string): Observable<DocumentRetrievedResource> {
    return this.http.get<DocumentRetrievedResource>(`${this.dmsServiceUrl}/${id}`);
  }

  /**
   * 1. 向 File Service 取得一般檔案的預先簽名網址 (Pre-signed URL)
   * @param fileName 檔案名稱
   * @param mimeType 檔案類型
   * @param size 檔案大小
   */
  getPresignedUploadUrl(fileName: string, mimeType: string, size: number): Observable<PresignedUrlResponse> {
    const payload = { originalFileName: fileName, mimeType, size, expiryMinutes: 15 };
    return this.http.post<PresignedUrlResponse>(`${this.fileServiceUrl}/presigned-upload`, payload);
  }

  /**
   * 2. 實體檔案上傳 (使用 Pre-signed URL 直接 PUT 至儲存服務/Bucket)
   * @param url Pre-signed URL
   * @param file 實體檔案或 Blob
   */
  uploadToStorage(url: string, file: File | Blob): Observable<any> {
    return this.http.put(url, file, {
      headers: {
        'Content-Type': file.type
      },
      observe: 'response' // 為了分段上傳能讀取 ETag
    });
  }

  /**
   * 初始化分段上傳 (Multipart Upload)
   */
  initiateMultipartUpload(fileName: string, mimeType: string, size: number): Observable<InitiateMultipartResult> {
    const payload = { originalFileName: fileName, mimeType, size };
    return this.http.post<InitiateMultipartResult>(`${this.fileServiceUrl}/multipart/initiate`, payload);
  }

  /**
   * 取得分段上傳的專屬預先簽名網址
   */
  getPresignedPartUrl(fileId: string, uploadId: string, partNumber: number): Observable<string> {
    return this.http.get(`${this.fileServiceUrl}/multipart/${fileId}/${uploadId}/presigned-part?partNumber=${partNumber}`, { responseType: 'text' });
  }

  /**
   * 完成分段上傳並合併檔案
   */
  completeMultipartUpload(fileId: string, uploadId: string, partETags: Record<number, string>): Observable<FileResponse> {
    const payload = { partETags };
    return this.http.post<FileResponse>(`${this.fileServiceUrl}/multipart/${fileId}/${uploadId}/complete`, payload);
  }

  /**
   * [棄用] 舊版直接上傳至 file-service 的方法
   * @deprecated 由於導入權限代理架構，應改用 getPresignedUploadUrl + uploadToStorage
   */
  uploadFile(file: File): Observable<FileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<FileResponse>(`${this.fileServiceUrl}/upload`, formData);
  }

  /**
   * 2. 在 dms-service 建立文件關聯
   * @param title 文件標題
   * @param description 文件描述
   * @param fileId 已上傳檔案的 ID
   * @returns 建立完成的文件結果
   */
  createDocument(title: string, description: string, fileId: string): Observable<DocumentCreatedResource> {
    const payload = {
      title,
      description,
      fileId
    };
    return this.http.post<DocumentCreatedResource>(this.dmsServiceUrl, payload);
  }

  /**
   * 取得文件列表
   */
  getDocuments(page: number, size: number, title?: string, status?: string): Observable<PageRetrievedResource<DocumentSearchedResource>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (title) params = params.set('title', title);
    if (status) params = params.set('status', status);

    return this.http.get<PageRetrievedResource<DocumentSearchedResource>>(this.dmsServiceUrl, { params });
  }

  /**
   * 更新文件
   */
  updateDocument(id: string, title: string, description: string): Observable<DocumentCreatedResource> {
    const payload = { title, description };
    return this.http.put<DocumentCreatedResource>(`${this.dmsServiceUrl}/${id}`, payload);
  }

  /**
   * 上傳新版本文件 (綁定新實體檔案)
   * @param id 文件 ID
   * @param fileId 新的實體檔案 ID
   * @param isMajorVersion 是否為主版本更新
   */
  uploadDocumentVersion(id: string, fileId: string, isMajorVersion: boolean): Observable<DocumentCreatedResource> {
    const payload = { fileId, isMajorVersion };
    return this.http.post<DocumentCreatedResource>(`${this.dmsServiceUrl}/${id}/versions`, payload);
  }

  /**
   * 刪除文件
   */
  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.dmsServiceUrl}/${id}`);
  }

  /**
   * 取得檔案的預先簽名下載網址 (強制下載)
   * @param fileId 檔案 ID
   */
  getPresignedDownloadUrl(fileId: string): Observable<string> {
    return this.http.get(`${this.fileServiceUrl}/${fileId}/presigned-download?isDownload=true`, { responseType: 'text' });
  }

  /**
   * 取得檔案的預先簽名預覽網址 (瀏覽器內嵌開啟)
   * @param fileId 檔案 ID
   */
  getPresignedPreviewUrl(fileId: string): Observable<string> {
    return this.http.get(`${this.fileServiceUrl}/${fileId}/presigned-download?isDownload=false`, { responseType: 'text' });
  }
}
