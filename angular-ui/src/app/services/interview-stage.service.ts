import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InterviewStageDTO } from '../models/interview-stage.model';
import { Page, PageRequest } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class InterviewStageService {
  private baseUrl = '/api/interview-stages';

  constructor(private http: HttpClient) {}

  getAll(): Observable<InterviewStageDTO[]> {
    return this.http.get<InterviewStageDTO[]>(this.baseUrl);
  }

  getAllPaged(pageRequest: PageRequest): Observable<Page<InterviewStageDTO>> {
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

    return this.http.get<Page<InterviewStageDTO>>(`${this.baseUrl}/page`, { params });
  }

  getById(id: string): Observable<InterviewStageDTO> {
    return this.http.get<InterviewStageDTO>(`${this.baseUrl}/${id}`);
  }

  getByCode(code: string): Observable<InterviewStageDTO> {
    return this.http.get<InterviewStageDTO>(`${this.baseUrl}/by-code/${code}`);
  }

  getByCategory(categoryId: string): Observable<InterviewStageDTO[]> {
    return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/by-category/${categoryId}`);
  }

  getActive(): Observable<InterviewStageDTO[]> {
    return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/active`);
  }

  getFinalStages(): Observable<InterviewStageDTO[]> {
    return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/final`);
  }

  getOrderedStages(): Observable<InterviewStageDTO[]> {
    return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/ordered`);
  }

  create(stage: InterviewStageDTO): Observable<InterviewStageDTO> {
    return this.http.post<InterviewStageDTO>(this.baseUrl, stage);
  }

  update(id: string, stage: InterviewStageDTO): Observable<InterviewStageDTO> {
    return this.http.put<InterviewStageDTO>(`${this.baseUrl}/${id}`, stage);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  updateOrder(stageOrders: { id: string, orderIndex: number }[]): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/update-order`, stageOrders);
  }
}
