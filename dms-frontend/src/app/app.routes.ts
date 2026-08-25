import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(c => c.MainLayoutComponent),
    children: [
      { path: 'upload', loadComponent: () => import('./features/document-upload/document-upload.component').then(c => c.DocumentUploadComponent) },
      { path: 'documents', loadComponent: () => import('./features/document-list/document-list.component').then(c => c.DocumentListComponent) },
      { path: 'templates', loadComponent: () => import('./features/template-summary/template-summary.component').then(m => m.TemplateSummaryComponent) },
      { path: 'template-editor/:id', loadComponent: () => import('./features/template-editor/template-editor.component').then(m => m.TemplateEditorComponent) },
      { path: 'template-fill/:id', loadComponent: () => import('./features/template-fill/template-fill.component').then(m => m.TemplateFillComponent) },
      { path: 'validation-policies', loadComponent: () => import('./features/validation-policy/validation-policy.component').then(m => m.ValidationPolicyComponent) },
      { path: 'template-field-mappings/:templateCode', loadComponent: () => import('./features/template-field-mapping/template-field-mapping.component').then(m => m.TemplateFieldMappingComponent) },
      { path: '', redirectTo: 'documents', pathMatch: 'full' }
    ]
  }
];
