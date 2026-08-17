import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ConfirmationService, MessageService, MenuItem } from 'primeng/api';
import { DocumentService } from '../../services/document.service';
import { DocumentSearchedResource, DocumentVersionResource } from '../../core/models/document.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { MenuModule } from 'primeng/menu';
import { FileUploadModule } from 'primeng/fileupload';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

/**
 * 文件列表元件 (Document List Component)
 * 負責呈現 DMS 系統中的文件總表，包含支援分頁、狀態呈現，以及整合了預覽、下載、編輯與刪除等核心操作。
 */
@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    ConfirmDialogModule,
    ToastModule,
    TagModule,
    InputTextModule,
    InputTextareaModule,
    FileUploadModule,
    CheckboxModule,
    MenuModule,
    DropdownModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css'
})
export class DocumentListComponent implements OnInit, OnDestroy {
  /** 儲存當前頁面的文件資料陣列 */
  documents: DocumentSearchedResource[] = [];
  /** 用於 Paginator 計算總頁數的總資料筆數 */
  totalRecords: number = 0;
  /** 控制表格讀取中的遮罩狀態 */
  loading: boolean = true;

  /** 控制編輯彈窗的顯示與隱藏 */
  editDialogVisible: boolean = false;
  /** 暫存當前正在編輯的文件資料 */
  editingDocument: any = { id: '', title: '', description: '' };

  /** 上傳新版本彈窗狀態 */
  uploadVersionDialogVisible: boolean = false;
  versionToUpload: File | null = null;
  isMajorVersion: boolean = false;
  uploadingVersion: boolean = false;
  targetVersionDoc: DocumentSearchedResource | null = null;

  /** 查看歷史紀錄彈窗狀態 */
  historyDialogVisible: boolean = false;
  selectedDocumentHistory: DocumentVersionResource[] = [];

  /** 表格操作選單 */
  actionDoc: DocumentSearchedResource | null = null;
  actionMenuItems: MenuItem[] = [];

  /** 搜尋表單狀態 */
  searchTitle: string = '';
  searchStatus: string | undefined = undefined;
  titleSearchSubject: Subject<string> = new Subject<string>();
  titleSearchSubscription!: Subscription;

  statusOptions = [
    { label: '全部狀態', value: undefined },
    { label: '草稿', value: 'DRAFT' },
    { label: '發布', value: 'PUBLISHED' },
    { label: '封存', value: 'ARCHIVED' },
    { label: '刪除', value: 'DELETED' }
  ];

  constructor(
    private documentService: DocumentService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.titleSearchSubscription = this.titleSearchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(value => {
      const trimmedValue = value.trim();
      if (trimmedValue.length >= 3 || trimmedValue.length === 0) {
        this.resetAndLoadDocuments();
      }
    });

    this.actionMenuItems = [
      { label: '預覽', icon: 'pi pi-eye', command: () => this.actionDoc && this.previewDocument(this.actionDoc) },
      { label: '下載', icon: 'pi pi-download', command: () => this.actionDoc && this.downloadDocument(this.actionDoc) },
      { label: '編輯', icon: 'pi pi-pencil', command: () => this.actionDoc && this.openEditDialog(this.actionDoc) },
      { label: '上傳新版本', icon: 'pi pi-upload', command: () => this.actionDoc && this.uploadNewVersion(this.actionDoc) },
      { label: '歷史版本', icon: 'pi pi-history', command: () => this.actionDoc && this.viewHistory(this.actionDoc) },
      { separator: true },
      { label: '刪除', icon: 'pi pi-trash', styleClass: 'text-red-500', command: () => this.actionDoc && this.confirmDelete(this.actionDoc) }
    ];
  }

  ngOnDestroy() {
    if (this.titleSearchSubscription) {
      this.titleSearchSubscription.unsubscribe();
    }
  }

  onTitleSearchChange(value: string) {
    this.titleSearchSubject.next(value);
  }

  onStatusChange() {
    this.resetAndLoadDocuments();
  }

  resetAndLoadDocuments() {
    this.loadDocuments({ first: 0, rows: 10 });
  }

  openActionMenu(event: Event, menu: any, doc: DocumentSearchedResource) {
    this.actionDoc = doc;
    menu.toggle(event);
  }

  /**
   * P-Table 的 Lazy Load 回呼函式。
   * 當使用者翻頁、改變每頁筆數或元件初次載入時會觸發此方法，向後端索取分頁資料。
   * @param event PrimeNG Table 提供的 LazyLoad 事件物件
   */
  loadDocuments(event: TableLazyLoadEvent) {
    this.loading = true;
    const page = event.first !== undefined && event.rows ? event.first / event.rows : 0;
    const size = event.rows ? event.rows : 10;

    const titleParam = this.searchTitle.trim().length >= 3 ? this.searchTitle.trim() : undefined;
    const statusParam = this.searchStatus;

    this.documentService.getDocuments(page, size, titleParam, statusParam).subscribe({
      next: (res) => {
        this.documents = res.content;
        this.totalRecords = res.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '無法載入文件列表' });
        this.loading = false;
      }
    });
  }

  /**
   * 根據文件的狀態字串回傳對應的 PrimeNG Tag Severity 顏色主題。
   * @param status 文件的業務狀態
   */
  getSeverity(status: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast' | undefined {
    switch (status) {
      case 'PUBLISHED': return 'success';
      case 'DRAFT': return 'warning';
      case 'DELETED': return 'danger';
      case 'ARCHIVED': return 'secondary';
      default: return 'info';
    }
  }

  /**
   * 打開編輯文件的彈窗，並深拷貝文件資料以免直接污染列表資料。
   * @param doc 要編輯的文件資源
   */
  openEditDialog(doc: DocumentSearchedResource) {
    this.editingDocument = { ...doc };
    this.editDialogVisible = true;
  }

  /**
   * 儲存已編輯的文件中繼資料 (標題、描述)。
   */
  saveEdit() {
    this.documentService.updateDocument(this.editingDocument.id, this.editingDocument.title, this.editingDocument.description).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: '更新成功' });
        this.editDialogVisible = false;
        // Reload table
        this.loadDocuments({ first: 0, rows: 10 });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '更新失敗' });
      }
    });
  }

  /**
   * 觸發上傳新版本檔案的功能
   * @param doc 要更新版本的文件資源
   */
  uploadNewVersion(doc: DocumentSearchedResource) {
    this.targetVersionDoc = doc;
    this.versionToUpload = null;
    this.isMajorVersion = false;
    this.uploadVersionDialogVisible = true;
  }

  onVersionFileSelect(event: any) {
    if (event.files && event.files.length > 0) {
      this.versionToUpload = event.files[0];
    }
  }

  onVersionFileRemove(event: any) {
    this.versionToUpload = null;
  }

  async submitUploadVersion(fileUploader: any) {
    if (!this.versionToUpload || !this.targetVersionDoc) {
      this.messageService.add({ severity: 'warn', summary: '警告', detail: '請選擇要上傳的檔案' });
      return;
    }

    this.uploadingVersion = true;
    const file = this.versionToUpload;
    const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB

    try {
      let fileId: string;

      if (file.size <= CHUNK_SIZE) {
        // 小檔案
        const presignedResp = await lastValueFrom(this.documentService.getPresignedUploadUrl(file.name, file.type, file.size));
        if (!presignedResp || !presignedResp.url || !presignedResp.fileId) throw new Error('無法取得上傳憑證');
        await lastValueFrom(this.documentService.uploadToStorage(presignedResp.url, file));
        fileId = presignedResp.fileId;
      } else {
        // 大檔案 (Multipart)
        const initResp = await lastValueFrom(this.documentService.initiateMultipartUpload(file.name, file.type, file.size));
        if (!initResp || !initResp.uploadId || !initResp.fileId) throw new Error('無法初始化分段上傳');
        
        const partETags: Record<number, string> = {};
        const numParts = Math.ceil(file.size / CHUNK_SIZE);
        for (let i = 0; i < numParts; i++) {
          const partNumber = i + 1;
          const chunk = file.slice(i * CHUNK_SIZE, Math.min((i + 1) * CHUNK_SIZE, file.size));
          const partUrl = await lastValueFrom(this.documentService.getPresignedPartUrl(initResp.fileId, initResp.uploadId, partNumber));
          const uploadResp = await lastValueFrom(this.documentService.uploadToStorage(partUrl, chunk));
          const eTag = uploadResp.headers.get('ETag');
          partETags[partNumber] = eTag ? eTag.replace(/"/g, '') : 'dummy-etag';
        }
        await lastValueFrom(this.documentService.completeMultipartUpload(initResp.fileId, initResp.uploadId, partETags));
        fileId = initResp.fileId;
      }

      // 呼叫 DMS Service 綁定新版本
      await lastValueFrom(this.documentService.uploadDocumentVersion(this.targetVersionDoc.id, fileId, this.isMajorVersion));

      this.messageService.add({ severity: 'success', summary: '成功', detail: `文件新版本上傳成功` });
      this.uploadVersionDialogVisible = false;
      this.loadDocuments({ first: 0, rows: 10 });
      fileUploader.clear();

    } catch (err: any) {
      console.error(err);
      let errMsg = err.error?.message || err.message || '上傳失敗';
      this.messageService.add({ severity: 'error', summary: '上傳失敗', detail: errMsg });
    } finally {
      this.uploadingVersion = false;
    }
  }

  /**
   * 觸發查看歷史版本列表的功能
   * @param doc 要查看歷史的文件資源
   */
  viewHistory(doc: DocumentSearchedResource) {
    this.documentService.getDocument(doc.id).subscribe({
      next: (res) => {
        // 依照 createdAt 排序 (新版本在上面)
        this.selectedDocumentHistory = res.history.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.historyDialogVisible = true;
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '無法取得歷史紀錄' });
      }
    });
  }

  /**
   * 下載歷史版本實體檔案
   * @param fileId 歷史版本的實體檔案 ID
   */
  downloadHistoryVersion(fileId: string) {
    if (!fileId) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: '此版本沒有關聯的檔案' });
      return;
    }
    
    this.documentService.getPresignedDownloadUrl(fileId).subscribe({
      next: (url) => {
        const a = document.createElement('a');
        a.href = url;
        a.download = ''; 
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '無法取得下載連結' });
      }
    });
  }

  /**
   * 取得下載專用的 Pre-signed URL，並建立一個隱藏的 `<a>` 標籤自動觸發瀏覽器下載。
   * @param doc 要下載的文件資源
   */
  downloadDocument(doc: DocumentSearchedResource) {
    if (!doc.fileId) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: '此文件沒有關聯的檔案' });
      return;
    }
    
    this.documentService.getPresignedDownloadUrl(doc.fileId).subscribe({
      next: (url) => {
        const a = document.createElement('a');
        a.href = url;
        a.download = ''; // 讓瀏覽器根據 Content-Disposition 或 URL 自動決定檔名
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '無法取得下載連結' });
      }
    });
  }

  /**
   * 取得預覽專用的 Pre-signed URL，並開新分頁讓瀏覽器內建預覽器 (如 PDF 閱讀器) 開啟該檔案。
   * @param doc 要預覽的文件資源
   */
  previewDocument(doc: DocumentSearchedResource) {
    if (!doc.fileId) {
      this.messageService.add({ severity: 'warn', summary: 'Warning', detail: '此文件沒有關聯的檔案' });
      return;
    }
    
    this.documentService.getPresignedPreviewUrl(doc.fileId).subscribe({
      next: (url) => {
        window.open(url, '_blank');
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: '無法取得預覽連結' });
      }
    });
  }

  /**
   * 觸發 PrimeNG 的防呆確認彈窗，經使用者確認後呼叫 API 執行文件刪除。
   * @param doc 準備要刪除的文件資源
   */
  confirmDelete(doc: DocumentSearchedResource) {
    this.confirmationService.confirm({
      message: `確定要刪除文件 "${doc.title}" 嗎？`,
      header: '確認刪除',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger p-button-text',
      rejectButtonStyleClass: 'p-button-text',
      defaultFocus: 'reject',
      accept: () => {
        this.documentService.deleteDocument(doc.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: '刪除成功' });
            this.loadDocuments({ first: 0, rows: 10 });
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: '刪除失敗' });
          }
        });
      }
    });
  }
}
