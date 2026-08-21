import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TemplateService } from '../../core/services/template.service';

/**
 * 範本填寫元件 (Template Fill Component)
 * 負責根據範本定義的變數 (Excel 的 columns 或 PDF 的 blocks)
 * 動態產生對應的填寫表單。並提供 PDF 即時預覽與最終檔案的產生及下載功能。
 */
@Component({
  selector: 'app-template-fill',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule
  ],
  templateUrl: './template-fill.component.html',
  styleUrls: ['./template-fill.component.css']
})
export class TemplateFillComponent implements OnInit {

  // --- 範本基本資訊 ---
  templateId: string = '';
  templateName: string = '';
  templateType: string = '';

  // --- 動態表單資料 ---
  columns: any[] = [];
  formData: { [key: string]: any } = {};

  // --- 畫面狀態 ---
  isLoading = true;
  isDownloading = false;

  // --- PDF 預覽狀態 ---
  isPreviewLoading = false;
  previewUrl: SafeResourceUrl | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private templateService: TemplateService,
    private sanitizer: DomSanitizer,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.templateId = params.get('id') || '';
      if (this.templateId) {
        this.loadTemplate();
      }
    });
  }

  /**
   * 載入指定範本的詳細定義，並解析 JSON 結構以動態建立表單欄位。
   */
  loadTemplate() {
    this.isLoading = true;
    this.templateService.getTemplate(this.templateId).subscribe({
      next: (res) => {
        this.templateName = res.name;
        this.templateType = res.templateType;
        if (res.draftJson) {
          try {
            const parsed = JSON.parse(res.draftJson);
            if (this.templateType === 'PDF') {
              // PDF: 從 blocks 中自動萃取所有變數欄位 (field, leftField, rightField, imageField)
              const pdfCols: any[] = [];
              if (parsed.blocks && Array.isArray(parsed.blocks)) {
                parsed.blocks.forEach((b: any) => {
                  if (b.field) pdfCols.push({ field: b.field, header: `欄位: ${b.field}` });
                  if (b.leftField) pdfCols.push({ field: b.leftField, header: `左側欄位: ${b.leftField}` });
                  if (b.rightField) pdfCols.push({ field: b.rightField, header: `右側欄位: ${b.rightField}` });
                  if (b.imageField) pdfCols.push({ field: b.imageField, header: `圖片網址: ${b.imageField}`, isImage: true });
                });
              }
              this.columns = pdfCols;
            } else {
              // Excel: 依賴原本定義的 columns
              this.columns = parsed.columns || [];
            }
            
            // 初始化 formData，將每個變數預設為空字串
            this.columns.forEach(col => {
              if (col.field) {
                this.formData[col.field] = '';
              }
            });
          } catch (e) {
            console.error('Failed to parse draftJson', e);
          }
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load template:', err);
        this.isLoading = false;
      }
    });
  }

  /**
   * 將表單填寫的變數資料送到後端，產生最終的二進位檔案並觸發瀏覽器下載。
   */
  download() {
    this.isDownloading = true;
    this.templateService.fillAndDownloadTemplate(this.templateId, this.formData).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        // 根據範本類型決定下載附檔名
        const ext = this.templateType === 'PDF' ? 'pdf' : 'xlsx';
        a.download = `${this.templateName}_filled.${ext}`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.isDownloading = false;
      },
      error: (err) => {
        console.error('Download failed', err);
        this.isDownloading = false;
      }
    });
  }

  /**
   * 重新取得最新的 PDF 並顯示在右側的即時預覽區。
   */
  refreshPreview() {
    if (this.templateType !== 'PDF') return;
    
    this.isPreviewLoading = true;
    this.templateService.fillAndDownloadTemplate(this.templateId, this.formData).subscribe({
      next: (blob) => {
        this.isPreviewLoading = false;
        const url = window.URL.createObjectURL(blob);
        // 使用 DomSanitizer 確保該 Blob URL 可以在 <object> 中合法顯示
        this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      },
      error: (err) => {
        this.isPreviewLoading = false;
        console.error('Preview failed', err);
      }
    });
  }

  /**
   * 處理圖片上傳事件：透過 FileReader 將圖片轉為 Base64 字串，
   * 讓後端能夠直接渲染圖片而無須經過實體的檔案儲存服務。
   * 
   * @param event 檔案選擇事件
   * @param field 綁定的圖片變數名稱 (例如 image_4)
   */
  onImageUpload(event: any, field: string) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        // 將圖片轉為 Base64 (Data URL) 格式
        this.formData[field] = e.target.result;
        // 自動重新整理預覽
        this.refreshPreview();
      };
      reader.onerror = () => {
        console.error('Failed to read file');
        alert('圖片讀取失敗');
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * 返回上一頁 (總覽列表)
   */
  goBack() {
    this.router.navigate(['/templates']);
  }
}
