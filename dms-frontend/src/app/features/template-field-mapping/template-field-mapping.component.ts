import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TemplateFieldMappingService } from '../../services/template-field-mapping.service';
import { TemplateFieldMapping } from '../../models/template-field-mapping.model';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-template-field-mapping',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, DialogModule, InputTextModule, FormsModule, ReactiveFormsModule, ToastModule],
  providers: [MessageService],
  templateUrl: './template-field-mapping.component.html',
  styleUrls: ['./template-field-mapping.component.css']
})
export class TemplateFieldMappingComponent implements OnInit {

  mappings: TemplateFieldMapping[] = [];
  templateCode: string = '';
  displayDialog: boolean = false;
  mappingForm: FormGroup;
  isEditMode: boolean = false;
  editingId: number | null = null;

  constructor(
    private mappingService: TemplateFieldMappingService,
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private messageService: MessageService
  ) {
    this.mappingForm = this.fb.group({
      templateSheetName: [''],
      headerName: ['', Validators.required],
      mappingFieldName: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.templateCode = params.get('templateCode') || '';
      if (this.templateCode) {
        this.loadMappings();
      } else {
        this.messageService.add({ severity: 'error', summary: '錯誤', detail: '缺乏 templateCode 參數' });
      }
    });
  }

  loadMappings() {
    this.mappingService.getMappingsByTemplate(this.templateCode).subscribe({
      next: (data) => this.mappings = data,
      error: (err) => this.messageService.add({ severity: 'error', summary: '錯誤', detail: '載入失敗' })
    });
  }

  showAddDialog() {
    this.isEditMode = false;
    this.editingId = null;
    this.mappingForm.reset();
    this.displayDialog = true;
  }

  showEditDialog(mapping: TemplateFieldMapping) {
    this.isEditMode = true;
    this.editingId = mapping.id || null;
    this.mappingForm.patchValue({
      templateSheetName: mapping.templateSheetName,
      headerName: mapping.headerName,
      mappingFieldName: mapping.mappingFieldName
    });
    this.displayDialog = true;
  }

  hideDialog() {
    this.displayDialog = false;
  }

  saveMapping() {
    if (this.mappingForm.invalid) {
      this.mappingForm.markAllAsTouched();
      return;
    }

    const mappingData: TemplateFieldMapping = {
      templateCode: this.templateCode,
      templateSheetName: this.mappingForm.value.templateSheetName,
      headerName: this.mappingForm.value.headerName,
      mappingFieldName: this.mappingForm.value.mappingFieldName
    };

    if (this.isEditMode && this.editingId) {
      mappingData.id = this.editingId;
      this.mappingService.updateMapping(this.editingId, mappingData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '更新成功' });
          this.displayDialog = false;
          this.loadMappings();
        },
        error: () => this.messageService.add({ severity: 'error', summary: '錯誤', detail: '更新失敗' })
      });
    } else {
      this.mappingService.createMapping(mappingData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '新增成功' });
          this.displayDialog = false;
          this.loadMappings();
        },
        error: () => this.messageService.add({ severity: 'error', summary: '錯誤', detail: '新增失敗' })
      });
    }
  }

  deleteMapping(id: number) {
    if (confirm('確定要刪除這筆對應嗎？')) {
      this.mappingService.deleteMapping(id).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '刪除成功' });
          this.loadMappings();
        },
        error: () => this.messageService.add({ severity: 'error', summary: '錯誤', detail: '刪除失敗' })
      });
    }
  }

  goBack() {
    this.router.navigate(['/templates']);
  }
}
