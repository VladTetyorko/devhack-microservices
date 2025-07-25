import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VacancyDTO } from '../models/vacancy.model';
import { Page, PageRequest } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class VacancyService {
  private baseUrl = '/api/vacancies';

  constructor(private http: HttpClient) {}

  getAll(): Observable<VacancyDTO[]> {
    return this.http.get<VacancyDTO[]>(this.baseUrl);
  }

  getAllPaged(pageRequest: PageRequest): Observable<Page<VacancyDTO>> {
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

    return this.http.get<Page<VacancyDTO>>(`${this.baseUrl}/page`, { params });
  }

  getById(id: string): Observable<VacancyDTO> {
    return this.http.get<VacancyDTO>(`${this.baseUrl}/${id}`);
  }

  search(query?: string, location?: string, company?: string): Observable<VacancyDTO[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (location) params = params.set('location', location);
    if (company) params = params.set('company', company);

    return this.http.get<VacancyDTO[]>(`${this.baseUrl}/search`, { params });
  }

  getByCompany(company: string): Observable<VacancyDTO[]> {
    return this.http.get<VacancyDTO[]>(`${this.baseUrl}/by-company/${company}`);
  }

  getByLocation(location: string): Observable<VacancyDTO[]> {
    return this.http.get<VacancyDTO[]>(`${this.baseUrl}/by-location/${location}`);
  }

  getRecent(limit?: number): Observable<VacancyDTO[]> {
    let params = new HttpParams();
    if (limit) params = params.set('limit', limit.toString());

    return this.http.get<VacancyDTO[]>(`${this.baseUrl}/recent`, { params });
  }

  create(vacancy: VacancyDTO): Observable<VacancyDTO> {
    return this.http.post<VacancyDTO>(this.baseUrl, vacancy);
  }

  update(id: string, vacancy: VacancyDTO): Observable<VacancyDTO> {
    return this.http.put<VacancyDTO>(`${this.baseUrl}/${id}`, vacancy);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  parseVacancy(url: string): Observable<VacancyDTO> {
    const body = { url };
    return this.http.post<VacancyDTO>(`${this.baseUrl}/parse`, body);
  }
}
