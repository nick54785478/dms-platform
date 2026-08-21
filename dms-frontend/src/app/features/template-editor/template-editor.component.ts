import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { TemplateService } from '../../core/services/template.service';
import { ActivatedRoute } from '@angular/router';
import { DragDropModule, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-template-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DialogModule,
    DropdownModule,
    DragDropModule
  ],
  templateUrl: './template-editor.component.html',
  styleUrls: ['./template-editor.component.css']
})
export class TemplateEditorComponent implements OnInit {

  templateId: string = '';
  createdTemplate: any = null;
  isLoading = false;
  isPublishing = false;
  message = '';
  draftJson: string | null = null;

  // PDF Draft Data
  pdfConfig: { pageSettings: any, blocks: any[] } = {
    pageSettings: { size: 'A4', orientation: 'portrait', margin: '20mm' },
    blocks: []
  };
  
  // Preview State
  showPreviewDialog = false;
  previewUrl: SafeResourceUrl | null = null;
  isPreviewLoading = false;

  // Dropdown options
  pdfSizeOptions = [
    { label: 'A4', value: 'A4' },
    { label: 'Letter', value: 'Letter' }
  ];
  pdfBlockTypeOptions = [
    { label: '標題 (Header)', value: 'header' },
    { label: '副標題 (Subheader)', value: 'subheader' },
    { label: '內文 (Text)', value: 'text' },
    { label: '左右並排 (Split)', value: 'split' },
    { label: '圖片 (Image)', value: 'image' },
    { label: '表格 (Table)', value: 'table' },
    { label: '分頁符號 (Page Break)', value: 'pagebreak' },
    { label: '簽名 (Signature)', value: 'signature' }
  ];

  showEditorDialog = false;
  isSaving = false;
  columns: any[] = [];


  constructor(
    private templateService: TemplateService, 
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer
  ) {
  }
  
  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.templateId = id;
        this.loadTemplate();
      }
    });
  }

  loadTemplate() {
    this.isLoading = true;
    this.templateService.getTemplate(this.templateId).subscribe({
      next: (res) => {
        this.createdTemplate = res;
        this.isLoading = false;
        
        if (this.createdTemplate.templateType === 'PDF') {
          if (res.draftJson) {
             this.draftJson = res.draftJson;
             this.parsePdfJson();
          } else {
             this.pdfConfig = {
               pageSettings: { size: 'A4', orientation: 'portrait', margin: '20mm' },
               blocks: [
                 { type: 'header', content: '新文件 (New Document)', align: 'center' },
                 { type: 'text', content: '這是內文 (Content)...' }
               ]
             };
             this.updatePdfJson();
          }
        } else {
          if (res.draftJson) {
            try {
              const parsed = JSON.parse(res.draftJson);
              if (parsed.columns && Array.isArray(parsed.columns)) {
                this.columns = parsed.columns;
              }
            } catch (e) {
              console.error('Failed to parse draftJson', e);
            }
          }
          this.updateJson();
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.message = '❌ 讀取失敗: ' + err.message;
        console.error(err);
      }
    });
  }

  // EXCEL Methods
  addColumn() {
    this.columns.push({ header: '', field: '' });
    this.updateJson();
  }
  
  removeColumn(index: number) {
    this.columns.splice(index, 1);
    this.updateJson();
  }
  
  drop(event: CdkDragDrop<any[]>) {
    if (this.createdTemplate?.templateType === 'PDF') {
      moveItemInArray(this.pdfConfig.blocks, event.previousIndex, event.currentIndex);
      this.updatePdfJson();
    } else {
      moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
      this.updateJson();
    }
  }
  
  updateJson() {
    if (this.createdTemplate?.templateType !== 'PDF') {
      this.draftJson = JSON.stringify({ columns: this.columns }, null, 2);
    }
  }

  // PDF Methods
  addPdfBlock() {
    const num = this.pdfConfig.blocks.length + 1;
    this.pdfConfig.blocks.push({ type: 'text', field: `text_${num}` });
    this.updatePdfJson();
  }
  
  onBlockTypeChange(block: any, index: number) {
    const num = index + 1;
    if (block.type === 'split') {
      block.leftField = `left_${num}`;
      block.rightField = `right_${num}`;
    } else if (block.type === 'image') {
      block.imageField = `image_${num}`;
    } else if (['header', 'subheader', 'text'].includes(block.type)) {
      block.field = `${block.type}_${num}`;
    }
    this.updatePdfJson();
  }
  
  removePdfBlock(index: number) {
    this.pdfConfig.blocks.splice(index, 1);
    this.updatePdfJson();
  }

  
  updatePdfJson() {
    // 清除舊版的靜態資料欄位，並根據當前區塊類型清除無用的綁定變數
    const cleanedBlocks = this.pdfConfig.blocks.map(block => {
      const { content, leftContent, rightContent, imageUrl, ...cleanBlock } = block;
      
      if (cleanBlock.type === 'image') {
        delete cleanBlock.field;
        delete cleanBlock.leftField;
        delete cleanBlock.rightField;
      } else if (cleanBlock.type === 'split') {
        delete cleanBlock.field;
        delete cleanBlock.imageField;
      } else if (cleanBlock.type === 'pagebreak') {
        delete cleanBlock.field;
        delete cleanBlock.imageField;
        delete cleanBlock.leftField;
        delete cleanBlock.rightField;
      } else {
        delete cleanBlock.imageField;
        delete cleanBlock.leftField;
        delete cleanBlock.rightField;
      }
      
      return cleanBlock;
    });
    this.draftJson = JSON.stringify({ ...this.pdfConfig, blocks: cleanedBlocks }, null, 2);
  }
  
  parsePdfJson() {
    try {
      const parsed = JSON.parse(this.draftJson || '{}');
      this.pdfConfig.pageSettings = parsed.pageSettings || { size: 'A4' };
      this.pdfConfig.blocks = parsed.blocks || [];
    } catch (e) {
      console.error('Failed to parse PDF JSON', e);
    }
  }

  applyJson() {
    try {
      if (this.createdTemplate?.templateType === 'PDF') {
        this.parsePdfJson();
      } else {
        const parsed = JSON.parse(this.draftJson || '{}');
        if (parsed.columns && Array.isArray(parsed.columns)) {
          this.columns = parsed.columns;
        }
      }
      this.showEditorDialog = false;
      this.message = '✅ JSON 已成功套用至表單！';
    } catch (e) {
      this.message = '❌ JSON 格式錯誤無法套用！';
    }
  }

  saveDraft(callback?: () => void) {
    if (!this.createdTemplate) return;
    
    this.isSaving = true;
    this.message = '';
    const req = {
      contentDefinition: this.draftJson || '',
      variables: []
    };
    
    this.templateService.saveDraft(this.createdTemplate.id, req).subscribe({
      next: () => {
        this.isSaving = false;
        this.message = '✅ 草稿 JSON 已儲存！';
        this.templateService.getTemplate(this.createdTemplate.id).subscribe(res => {
          this.createdTemplate = res;
          if (callback) callback();
        });
      },
      error: (err) => {
        this.isSaving = false;
        this.message = '❌ 儲存失敗: ' + err.message;
        console.error(err);
      }
    });
  }

  previewPdf() {
    if (!this.createdTemplate) return;
    
    // First save the draft so the backend has the latest JSON
    this.saveDraft(() => {
      this.isPreviewLoading = true;
      this.showPreviewDialog = true;
      
      // Dummy data for preview rendering
      const dummyData = {
        invoiceData: [
          { item: 'Consulting', amount: 1500 },
          { item: 'Development', amount: 3000 }
        ]
      };
      
      this.templateService.fillAndDownloadTemplate(this.createdTemplate.id, dummyData).subscribe({
        next: (blob) => {
          this.isPreviewLoading = false;
          const url = window.URL.createObjectURL(blob);
          this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        },
        error: (err) => {
          this.isPreviewLoading = false;
          this.message = '❌ 預覽載入失敗';
          console.error(err);
        }
      });
    });
  }



  publish() {
    if (!this.createdTemplate) return;
    
    this.isPublishing = true;
    this.message = '';
    
    // 如果沒有 latestVersion 就預設為 V1.0-DRAFT (兼容初次)
    const versionToPublish = this.createdTemplate.latestVersion || 'V1.0-DRAFT';
    
    this.templateService.publishTemplate(this.createdTemplate.id, versionToPublish).subscribe({
      next: () => {
        this.isPublishing = false;
        this.message = '🚀 範本已成功發佈 (釘板)！';
        this.templateService.getTemplate(this.createdTemplate.id).subscribe(res => {
          this.createdTemplate = res;
        });
      },
      error: (err) => {
        this.isPublishing = false;
        this.message = '❌ 發佈失敗: ' + err.message;
        console.error(err);
      }
    });
  }

  downloadExcel() {
    if (!this.createdTemplate) return;
    this.templateService.downloadTemplate(this.createdTemplate.id);
  }
}
