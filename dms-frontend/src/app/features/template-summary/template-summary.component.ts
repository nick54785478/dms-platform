import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { Router } from '@angular/router';
import { TemplateService } from '../../core/services/template.service';
import { TooltipModule } from 'primeng/tooltip';
import { TagModule } from 'primeng/tag';

/**
 * 範本維護總覽元件 (Template Summary Component)
 * 負責顯示範本列表、提供搜尋過濾、新增範本，以及跳轉至編輯/填寫頁面。
 * 支援懶加載分頁 (Lazy Loading) 與歷史版本檢視。
 */
@Component({
  selector: 'app-template-summary',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DialogModule,
    DropdownModule,
    TooltipModule,
    TagModule
  ],
  templateUrl: './template-summary.component.html',
  styleUrls: ['./template-summary.component.css']
})
export class TemplateSummaryComponent implements OnInit {
  // --- Query Form (搜尋表單狀態) ---
  searchParams = {
    templateType: '',
    templateCode: '',
    name: ''
  };

  // --- Table Data (資料表狀態) ---
  templates: any[] = [];
  totalRecords = 0;
  loading = false;
  
  // --- Create Dialog (新增範本對話框狀態) ---
  displayCreateDialog = false;
  newTemplate = {
    templateType: '',
    templateCode: '',
    name: '',
    description: ''
  };
  templateTypes = [
    { label: 'Excel 範本 (EXCEL)', value: 'EXCEL' },
    { label: 'PDF 範本 (PDF)', value: 'PDF' }
  ];
  isCreating = false;

  constructor(private templateService: TemplateService, private router: Router) {}

  ngOnInit(): void {
    // initial load will be triggered by table lazy load event
  }

  /**
   * PrimeNG 資料表的懶加載事件處理
   * @param event 包含分頁與排序資訊的事件物件
   */
  loadTemplates(event: any) {
    this.loading = true;
    
    // 計算分頁資訊 (PrimeNG table lazy event)
    const page = Math.floor(event.first / event.rows);
    const size = event.rows;

    const params = {
      ...this.searchParams,
      page: page,
      size: size
    };

    this.templateService.searchTemplates(params).subscribe({
      next: (res) => {
        this.templates = res.content;
        this.totalRecords = res.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load templates:', err);
        this.loading = false;
      }
    });
  }

  /**
   * 觸發資料搜尋 (重置回第一頁)
   */
  search() {
    // 透過傳遞假的 event 物件來觸發 loadTemplates，從第 0 筆開始抓取 10 筆
    this.loadTemplates({ first: 0, rows: 10 });
  }

  /**
   * 重置搜尋條件並重新載入列表
   */
  reset() {
    this.searchParams = {
      templateType: '',
      templateCode: '',
      name: ''
    };
    this.search();
  }

  /**
   * 顯示新增範本對話框並初始化表單
   */
  showCreateDialog() {
    this.newTemplate = {
      templateType: '',
      templateCode: '',
      name: '',
      description: ''
    };
    this.displayCreateDialog = true;
  }

  /**
   * 送出新增範本請求
   */
  createTemplate() {
    this.isCreating = true;
    this.templateService.createTemplate(this.newTemplate).subscribe({
      next: (res) => {
        this.isCreating = false;
        this.displayCreateDialog = false;
        this.search(); // 新增成功後重新整理列表
      },
      error: (err) => {
        console.error('Failed to create template:', err);
        this.isCreating = false;
      }
    });
  }

  /**
   * 導航至範本編輯器頁面 (拖曳/定義版型)
   * @param id 範本 ID
   */
  editTemplate(id: string) {
    this.router.navigate(['/template-editor', id]);
  }

  /**
   * 導航至範本填寫頁面 (預覽與資料綁定)
   * @param id 範本 ID
   */
  fillTemplate(id: string) {
    this.router.navigate(['/template-fill', id]);
  }

  // --- History Dialog state (歷史版本對話框狀態) ---
  displayHistoryDialog = false;
  historyVersions: any[] = [];
  loadingHistory = false;
  totalHistoryRecords = 0;
  currentHistoryTemplateId = '';

  // --- Content Preview Dialog state (JSON 內容預覽對話框狀態) ---
  displayContentDialog = false;
  selectedVersionForView: any = null;

  /**
   * 顯示指定範本的歷史版本清單
   * @param templateId 範本 ID
   */
  viewHistory(templateId: string) {
    this.currentHistoryTemplateId = templateId;
    this.displayHistoryDialog = true;
    // 觸發懶加載取得第一頁
    this.loadHistory({ first: 0, rows: 10 });
  }

  /**
   * 載入歷史版本分頁資料
   * @param event 包含分頁資訊的事件物件
   */
  loadHistory(event: any) {
    if (!this.currentHistoryTemplateId) return;
    
    this.loadingHistory = true;
    const page = Math.floor(event.first / event.rows);
    const size = event.rows;

    this.templateService.getTemplateVersions(this.currentHistoryTemplateId, page, size).subscribe({
      next: (res) => {
        this.historyVersions = res.content;
        this.totalHistoryRecords = res.totalElements;
        this.loadingHistory = false;
      },
      error: (err) => {
        console.error('Failed to load history:', err);
        this.loadingHistory = false;
      }
    });
  }

  /**
   * 顯示特定歷史版本的詳細 JSON 定義
   * @param version 歷史版本物件
   */
  viewVersionContent(version: any) {
    this.selectedVersionForView = version;
    this.displayContentDialog = true;
  }
}
