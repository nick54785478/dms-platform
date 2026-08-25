import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TemplateFieldMapping } from '../models/template-field-mapping.model';

@Injectable({
  providedIn: 'root'
})
export class TemplateFieldMappingService {

  private apiUrl = 'http://localhost:8083/api/template-field-mappings';

  constructor(private http: HttpClient) { }

  getMappingsByTemplate(templateCode: string): Observable<TemplateFieldMapping[]> {
    return this.http.get<TemplateFieldMapping[]>(`${this.apiUrl}/template/${templateCode}`);
  }

  createMapping(mapping: TemplateFieldMapping): Observable<void> {
    return this.http.post<void>(this.apiUrl, mapping);
  }

  updateMapping(id: number, mapping: TemplateFieldMapping): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}`, mapping);
  }

  deleteMapping(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
