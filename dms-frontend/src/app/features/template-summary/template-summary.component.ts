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
    DropdownModule
  ],
  templateUrl: './template-summary.component.html',
  styleUrls: ['./template-summary.component.css']
})
export class TemplateSummaryComponent implements OnInit {
  
  // Query Form
  searchParams = {
    templateType: '',
    templateCode: '',
    name: ''
  };

  // Table Data
  templates: any[] = [];
  totalRecords = 0;
  loading = false;
  
  // Create Dialog
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

  loadTemplates(event: any) {
    this.loading = true;
    
    // PrimeNG table lazy event
    const page = event.first / event.rows;
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
        console.error(err);
        this.loading = false;
      }
    });
  }

  search() {
    // Trigger table reload by passing a dummy event with pagination reset
    this.loadTemplates({ first: 0, rows: 10 });
  }

  reset() {
    this.searchParams = {
      templateType: '',
      templateCode: '',
      name: ''
    };
    this.search();
  }

  showCreateDialog() {
    this.newTemplate = {
      templateType: '',
      templateCode: '',
      name: '',
      description: ''
    };
    this.displayCreateDialog = true;
  }

  createTemplate() {
    this.isCreating = true;
    this.templateService.createTemplate(this.newTemplate).subscribe({
      next: (res) => {
        this.isCreating = false;
        this.displayCreateDialog = false;
        this.search(); // Refresh list
      },
      error: (err) => {
        console.error(err);
        this.isCreating = false;
      }
    });
  }

  editTemplate(id: string) {
    this.router.navigate(['/template-editor', id]);
  }

  fillTemplate(id: string) {
    this.router.navigate(['/template-fill', id]);
  }
}
