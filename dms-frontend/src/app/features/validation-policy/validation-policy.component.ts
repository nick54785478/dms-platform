import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ValidationPolicyService } from '../../services/validation-policy.service';
import { ValidationPolicy, CreateValidationPolicyResource, UpdateValidationPolicyResource } from '../../models/validation-policy.model';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToolbarModule } from 'primeng/toolbar';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'app-validation-policy',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, DialogModule, ButtonModule, InputTextModule,
    ToolbarModule, ConfirmDialogModule, ToastModule, DropdownModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './validation-policy.component.html',
  styleUrls: ['./validation-policy.component.css']
})
export class ValidationPolicyComponent implements OnInit {

  policies: ValidationPolicy[] = [];
  policyDialog: boolean = false;
  policyForm!: FormGroup;
  isEditMode: boolean = false;
  currentPolicyId: number | null = null;
  loading: boolean = false;
  hideSearchUI: boolean = false;

  activeFlagOptions = [
    { label: '啟用 (Y)', value: 'Y' },
    { label: '停用 (N)', value: 'N' }
  ];

  typeOptions = [
    { label: '列驗證 (ROW)', value: 'ROW' },
    { label: '欄驗證 (COLUMN)', value: 'COLUMN' },
    { label: '表驗證 (SHEET)', value: 'SHEET' }
  ];

  searchCode: string = '';
  passedTemplateCode: string = '';
  passedTemplateName: string = '';

  availableMappings: any[] = [];
  sheetOptions: any[] = [];
  fieldOptions: any[] = [];

  constructor(
    private validationPolicyService: ValidationPolicyService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.route.queryParams.subscribe(params => {
      this.passedTemplateCode = params['templateCode'] || '';
      this.passedTemplateName = params['templateName'] || '';
      
      if (this.passedTemplateCode) {
        this.hideSearchUI = true;
        this.searchCode = this.passedTemplateCode;
        
        // Patch the form immediately so that initial values are set correctly
        this.policyForm.patchValue({
          code: this.passedTemplateCode,
          templateName: this.passedTemplateName
        });

        this.loadPolicies();
      }
    });

    this.policyForm.get('code')?.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(code => {
      if (code) {
        this.fetchMappings(code);
      } else {
        this.availableMappings = [];
        this.sheetOptions = [];
        this.fieldOptions = [];
      }
    });

    this.policyForm.get('templateSheetName')?.valueChanges.subscribe(sheetName => {
      this.updateFieldOptions(sheetName);
    });
  }

  fetchMappings(code: string) {
    this.validationPolicyService.getTemplateSheetNames(code).subscribe({
      next: (sheets) => {
        if (sheets && sheets.length > 0) {
          this.sheetOptions = sheets.map(s => ({ label: s, value: s }));
        } else {
          this.sheetOptions = [{ label: '無 (PDF或未設定)', value: '' }];
        }
        
        // After loading sheets, if we already have a sheet name selected (e.g. edit mode), fetch its fields
        const currentSheet = this.policyForm.get('templateSheetName')?.value;
        if (currentSheet) {
          this.updateFieldOptions(currentSheet);
        }
      },
      error: () => {
        this.sheetOptions = [{ label: '無 (PDF或未設定)', value: '' }];
      }
    });
  }

  updateFieldOptions(sheetName: string) {
    const code = this.policyForm.get('code')?.value;
    if (!code || !sheetName) {
      this.fieldOptions = [];
      return;
    }
    
    this.validationPolicyService.getTemplateFieldMappingsBySheet(code, sheetName).subscribe({
      next: (mappings) => {
        if (mappings && mappings.length > 0) {
          this.fieldOptions = mappings.map(m => ({
            label: `${m.headerName} (${m.mappingFieldName})`,
            value: m.mappingFieldName
          }));
        } else {
          this.fieldOptions = [];
        }
      },
      error: () => {
        this.fieldOptions = [];
      }
    });
  }

  initForm(): void {
    this.policyForm = this.fb.group({
      code: ['', Validators.required],
      templateName: ['', Validators.required],
      templateSheetName: ['', Validators.required],
      mappingFieldName: ['', Validators.required],
      type: ['ROW', Validators.required],
      rule: ['', Validators.required],
      expression: ['', Validators.required],
      errorMessage: ['', Validators.required],
      priorityNo: [1, [Validators.required, Validators.min(1)]],
      activeFlag: ['Y']
    });
  }

  loadPolicies(): void {
    this.loading = true;
    this.validationPolicyService.getPolicies(this.searchCode || undefined).subscribe({
      next: (data) => {
        // Sort by template name then priority
        this.policies = data.sort((a, b) => {
          if (a.templateName === b.templateName) {
             return a.priorityNo - b.priorityNo;
          }
          return a.templateName.localeCompare(b.templateName);
        });
        this.loading = false;
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: '錯誤', detail: '無法載入驗證規則' });
        this.loading = false;
      }
    });
  }

  openNew(): void {
    this.isEditMode = false;
    this.currentPolicyId = null;
    
    // Preserve the passed code and name if available, or keep current form values
    const currentCode = this.policyForm.get('code')?.value || this.passedTemplateCode;
    const currentName = this.policyForm.get('templateName')?.value || this.passedTemplateName;
    
    this.policyForm.reset({ 
      code: currentCode,
      templateName: currentName,
      type: 'ROW', 
      priorityNo: 1, 
      activeFlag: 'Y' 
    });
    
    this.policyDialog = true;
  }

  editPolicy(policy: ValidationPolicy): void {
    this.isEditMode = true;
    this.currentPolicyId = policy.id;
    this.policyForm.patchValue({
      code: policy.code,
      templateName: policy.templateName,
      templateSheetName: policy.templateSheetName,
      mappingFieldName: policy.mappingFieldName,
      type: policy.type,
      rule: policy.rule,
      expression: policy.expression,
      errorMessage: policy.errorMessage,
      priorityNo: policy.priorityNo,
      activeFlag: policy.activeFlag
    });
    this.fetchMappings(policy.code);
    this.policyDialog = true;
  }

  deletePolicy(policy: ValidationPolicy): void {
    this.confirmationService.confirm({
      message: '確定要刪除這筆驗證規則嗎？',
      header: '確認刪除',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.validationPolicyService.deletePolicy(policy.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: '成功', detail: '驗證規則已刪除' });
            this.loadPolicies();
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: '錯誤', detail: '刪除失敗' });
          }
        });
      }
    });
  }

  hideDialog(): void {
    this.policyDialog = false;
  }

  savePolicy(): void {
    if (this.policyForm.invalid) {
      this.policyForm.markAllAsTouched();
      return;
    }

    const formValue = this.policyForm.value;

    if (this.isEditMode && this.currentPolicyId) {
      const updateData: UpdateValidationPolicyResource = { ...formValue };
      this.validationPolicyService.updatePolicy(this.currentPolicyId, updateData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '驗證規則已更新' });
          this.policyDialog = false;
          this.loadPolicies();
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: '錯誤', detail: '更新失敗' });
        }
      });
    } else {
      const createData: CreateValidationPolicyResource = {
        code: formValue.code,
        templateName: formValue.templateName,
        templateSheetName: formValue.templateSheetName,
        mappingFieldName: formValue.mappingFieldName,
        type: formValue.type,
        rule: formValue.rule,
        expression: formValue.expression,
        errorMessage: formValue.errorMessage,
        priorityNo: formValue.priorityNo
      };
      
      this.validationPolicyService.createPolicy(createData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '驗證規則已建立' });
          this.policyDialog = false;
          this.loadPolicies();
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: '錯誤', detail: '建立失敗' });
        }
      });
    }
  }

  triggerUploadTest(fileInput: any): void {
    if (!this.searchCode && !this.passedTemplateCode) {
      this.messageService.add({ severity: 'warn', summary: '警告', detail: '請先輸入並搜尋代碼，才能測試該代碼的驗證' });
      return;
    }
    fileInput.click();
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      const code = this.searchCode || this.passedTemplateCode;
      this.loading = true;
      this.validationPolicyService.testExcelValidation(code, file).subscribe({
        next: (res) => {
          this.loading = false;
          
          if (res && res.code === 'VALIDATED_FAILED' || res.code === 'VALIDATE_FAILED') {
            if (res.messages && Array.isArray(res.messages)) {
              // Show all errors as separate toasts
              const msgs = res.messages.map((msg: string) => ({
                severity: 'error', 
                summary: '資料檢核有誤', 
                detail: msg, 
                life: 10000 
              }));
              this.messageService.addAll(msgs);
            } else {
              this.messageService.add({ severity: 'error', summary: '驗證失敗', detail: '檔案檢核失敗' });
            }
          } else {
            this.messageService.add({ severity: 'success', summary: '驗證成功', detail: res.message || '檔案檢核無誤' });
          }
          
          // clear input
          event.target.value = '';
        },
        error: (err) => {
          this.loading = false;
          const errorMsg = err.error?.message || '檔案檢核失敗';
          this.messageService.add({ severity: 'error', summary: '驗證失敗', detail: errorMsg });
          // clear input
          event.target.value = '';
        }
      });
    }
  }

  downloadTestTemplate(): void {
    const code = this.searchCode || this.passedTemplateCode;
    if (!code) {
      this.messageService.add({ severity: 'warn', summary: '警告', detail: '請先輸入並搜尋代碼，才能下載該代碼的範本' });
      return;
    }

    this.loading = true;
    this.validationPolicyService.getTemplateByCode(code).subscribe({
      next: (res: any) => {
        if (res.content && res.content.length > 0) {
          const templateId = res.content[0].id;
          this.validationPolicyService.downloadTemplate(templateId).subscribe({
            next: (blob: any) => {
              this.loading = false;
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `${code}_test_template.xlsx`;
              document.body.appendChild(a);
              a.click();
              document.body.removeChild(a);
              window.URL.revokeObjectURL(url);
              this.messageService.add({ severity: 'success', summary: '成功', detail: '範本下載完成' });
            },
            error: () => {
              this.loading = false;
              this.messageService.add({ severity: 'error', summary: '錯誤', detail: '下載範本失敗' });
            }
          });
        } else {
          this.loading = false;
          this.messageService.add({ severity: 'error', summary: '錯誤', detail: '找不到對應的範本' });
        }
      },
      error: () => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: '錯誤', detail: '查詢範本失敗' });
      }
    });
  }
}
