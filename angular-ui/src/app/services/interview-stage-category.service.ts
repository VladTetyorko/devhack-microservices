import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InterviewStageCategoryDTO } from '../models/interview-stage-category.model';
import { Page, PageRequest } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class InterviewStageCategoryService {
  private baseUrl = '/api/interview-stage-categories';

  constructor(private http: HttpClient) {}

  getAll(): Observable<InterviewStageCategoryDTO[]> {
    return this.http.get<InterviewStageCategoryDTO[]>(this.baseUrl);
  }

  getAllPaged(pageRequest: PageRequest): Observable<Page<InterviewStageCategoryDTO>> {
    let params = new HttpParams();

    if (pageRequest.page !== undefined) {
      params = params.set('page', pageRequest.page.toString());
    }
    if (pageRequest.size !== undefined) {
      params = params.set('size', pageRequest.size.toString());
    }
    if (pageRequest.sort && pageRequest.sort.length > 0) {
      pageRequest.sort.forEach(sortParam => {
        params = params.append('sort', sortParam);
      });
    }

    return this.http.get<Page<InterviewStageCategoryDTO>>(`${this.baseUrl}/page`, { params });
  }

  getById(id: string): Observable<InterviewStageCategoryDTO> {
    return this.http.get<InterviewStageCategoryDTO>(`${this.baseUrl}/${id}`);
  }

  getByCode(code: string): Observable<InterviewStageCategoryDTO> {
    return this.http.get<InterviewStageCategoryDTO>(`${this.baseUrl}/by-code/${code}`);
  }

  create(category: InterviewStageCategoryDTO): Observable<InterviewStageCategoryDTO> {
    return this.http.post<InterviewStageCategoryDTO>(this.baseUrl, category);
  }

  update(id: string, category: InterviewStageCategoryDTO): Observable<InterviewStageCategoryDTO> {
    return this.http.put<InterviewStageCategoryDTO>(`${this.baseUrl}/${id}`, category);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
