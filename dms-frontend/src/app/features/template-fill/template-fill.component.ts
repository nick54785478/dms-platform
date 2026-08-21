import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TemplateService } from '../../core/services/template.service';

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

  templateId: string = '';
  templateName: string = '';
  columns: any[] = [];
  formData: { [key: string]: any } = {};
  isLoading = true;
  isDownloading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private templateService: TemplateService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.templateId = params.get('id') || '';
      if (this.templateId) {
        this.loadTemplate();
      }
    });
  }

  loadTemplate() {
    this.isLoading = true;
    this.templateService.getTemplate(this.templateId).subscribe({
      next: (res) => {
        this.templateName = res.name;
        if (res.draftJson) {
          try {
            const parsed = JSON.parse(res.draftJson);
            this.columns = parsed.columns || [];
            // Initialize formData with empty strings for each field
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
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  download() {
    this.isDownloading = true;
    this.templateService.fillAndDownloadTemplate(this.templateId, this.formData).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.templateName}_filled.xlsx`;
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

  goBack() {
    this.router.navigate(['/templates']);
  }
}
