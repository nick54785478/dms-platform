import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ValidationPolicy, CreateValidationPolicyResource, UpdateValidationPolicyResource } from '../models/validation-policy.model';


@Injectable({
  providedIn: 'root'
})
export class ValidationPolicyService {

  // validation-service is on port 8083
  private apiUrl = 'http://localhost:8083/validation-policies'; 

  constructor(private http: HttpClient) { }

  getPolicies(code?: string): Observable<ValidationPolicy[]> {
    let params = new HttpParams();
    if (code) {
      params = params.set('code', code);
    }
    return this.http.get<ValidationPolicy[]>(this.apiUrl, { params });
  }

  getTemplateFieldMappings(templateCode: string): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8083/api/template-field-mappings/template/${templateCode}`);
  }

  getTemplateSheetNames(templateCode: string): Observable<string[]> {
    return this.http.get<string[]>(`http://localhost:8083/api/template-field-mappings/template/${templateCode}/sheets`);
  }

  getTemplateFieldMappingsBySheet(templateCode: string, sheetName: string): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8083/api/template-field-mappings/template/${templateCode}/sheets/${sheetName}/fields`);
  }

  getPolicy(id: number): Observable<ValidationPolicy> {
    return this.http.get<ValidationPolicy>(`${this.apiUrl}/${id}`);
  }

  createPolicy(policy: CreateValidationPolicyResource): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(this.apiUrl, policy);
  }

  updatePolicy(id: number, policy: UpdateValidationPolicyResource): Observable<{ success: boolean }> {
    return this.http.put<{ success: boolean }>(`${this.apiUrl}/${id}`, policy);
  }

  deletePolicy(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  testExcelValidation(code: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('code', code);
    formData.append('file', file);
    return this.http.post<any>(`http://localhost:8083/api/v1/validation/excel`, formData);
  }

  getTemplateByCode(code: string): Observable<any> {
    return this.http.get<any>(`http://localhost:8082/api/v1/templates?templateCode=${code}`);
  }

  downloadTemplate(id: string): Observable<Blob> {
    return this.http.post(`http://localhost:8082/api/v1/templates/${id}/download`, {}, { responseType: 'blob' });
  }
}
