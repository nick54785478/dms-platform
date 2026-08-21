import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateTemplateRequest {
  templateType: string;
  templateCode: string;
  name: string;
  description: string;
}

export interface SaveTemplateDraftRequest {
  contentDefinition: string;
  variables: any[];
}

export interface TemplateResult {
  id: string;
  templateType: string;
  templateCode: string;
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {

  private readonly API_URL = 'http://localhost:8082/api/v1/templates';

  constructor(private http: HttpClient) { }

  createTemplate(request: CreateTemplateRequest): Observable<TemplateResult> {
    return this.http.post<TemplateResult>(this.API_URL, request);
  }

  saveDraft(id: string, request: SaveTemplateDraftRequest): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${id}/draft`, request);
  }

  publishTemplate(templateId: string, version: string): Observable<any> {
    return this.http.put(`${this.API_URL}/${templateId}/versions/${version}/publish`, {});
  }

  searchTemplates(params: any): Observable<any> {
    return this.http.get<any>(this.API_URL, { params });
  }

  getTemplate(id: string): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}`);
  }

  downloadTemplate(id: string): void {
    // Old implementation kept for reference, but POST is better now
    this.http.post(`${this.API_URL}/${id}/download`, {}, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `template_${id}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  fillAndDownloadTemplate(id: string, data: any): Observable<Blob> {
    return this.http.post(`${this.API_URL}/${id}/download`, data, { responseType: 'blob' });
  }

  getTemplateVersions(id: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}/versions`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }
}
