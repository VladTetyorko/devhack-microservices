import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TagDTO } from '../models/global/tag.model';
import { Page, PageRequest } from '../models/basic/page.model';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private baseUrl = '/api/tags';

  constructor(private http: HttpClient) {}

  getAll(): Observable<TagDTO[]> {
    return this.http.get<TagDTO[]>(this.baseUrl);
  }

  getAllPaged(pageRequest: PageRequest): Observable<Page<TagDTO>> {
    let params = new HttpParams();

    if (pageRequest.page !== undefined) {
      params = params.set('page', pageRequest.page.toString());
    }
    if (pageRequest.size !== undefined) {
      params = params.set('size', pageRequest.size.toString());
    }
    if (pageRequest.sort && pageRequest.sort.length > 0) {
      pageRequest.sort.forEach((sortParam: string) => {
        params = params.append('sort', sortParam);
      });
    }

    return this.http.get<Page<TagDTO>>(`${this.baseUrl}/page`, { params });
  }

  getById(id: string): Observable<TagDTO> {
    return this.http.get<TagDTO>(`${this.baseUrl}/${id}`);
  }

  getByName(name: string): Observable<TagDTO> {
    return this.http.get<TagDTO>(`${this.baseUrl}/by-name/${name}`);
  }

  search(query: string): Observable<TagDTO[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<TagDTO[]>(`${this.baseUrl}/search`, { params });
  }

  getPopular(limit?: number): Observable<TagDTO[]> {
    let params = new HttpParams();
    if (limit) params = params.set('limit', limit.toString());

    return this.http.get<TagDTO[]>(`${this.baseUrl}/popular`, { params });
  }

  create(tag: TagDTO): Observable<TagDTO> {
    return this.http.post<TagDTO>(this.baseUrl, tag);
  }

  update(id: string, tag: TagDTO): Observable<TagDTO> {
    return this.http.put<TagDTO>(`${this.baseUrl}/${id}`, tag);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getTagsWithQuestionCount(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/with-question-count`);
  }
}
