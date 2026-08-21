import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { TemplateService } from '../../core/services/template.service';
import { ActivatedRoute } from '@angular/router';
import { DragDropModule, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-template-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
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

  // JSON Draft Data
  draftJson: string = '';
  columns: any[] = [];

  // States
  isSaving = false;
  isPublishing = false;
  isLoading = false;
  message = '';
  showEditorDialog = false;

  constructor(private templateService: TemplateService, private route: ActivatedRoute) {
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
        
        // Restore Draft JSON to columns
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
      },
      error: (err) => {
        this.isLoading = false;
        this.message = '❌ 讀取失敗: ' + err.message;
        console.error(err);
      }
    });
  }

  addColumn() {
    this.columns.push({ header: '', field: '' });
    this.updateJson();
  }
  
  removeColumn(index: number) {
    this.columns.splice(index, 1);
    this.updateJson();
  }
  
  drop(event: CdkDragDrop<any[]>) {
    moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
    this.updateJson();
  }
  
  updateJson() {
    this.draftJson = JSON.stringify({ columns: this.columns }, null, 2);
  }

  applyJson() {
    try {
      const parsed = JSON.parse(this.draftJson);
      if (parsed.columns && Array.isArray(parsed.columns)) {
        this.columns = parsed.columns;
      }
      this.showEditorDialog = false;
      this.message = '✅ JSON 已成功套用至表單！';
    } catch (e) {
      this.message = '❌ JSON 格式錯誤無法套用！';
    }
  }

  saveDraft() {
    if (!this.createdTemplate) return;
    
    this.isSaving = true;
    this.message = '';
    const req = {
      contentDefinition: this.draftJson,
      variables: []
    };
    
    this.templateService.saveDraft(this.createdTemplate.id, req).subscribe({
      next: () => {
        this.isSaving = false;
        this.message = '✅ 草稿 JSON 已儲存！';
        // 儲存後重新向後端拉取最新範本狀態 (確保 latestVersion 被更新，例如從 V2.0-DRAFT 變成 V3.0-DRAFT)
        this.templateService.getTemplate(this.createdTemplate.id).subscribe(res => {
          this.createdTemplate = res;
        });
      },
      error: (err) => {
        this.isSaving = false;
        this.message = '❌ 儲存失敗: ' + err.message;
        console.error(err);
      }
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
