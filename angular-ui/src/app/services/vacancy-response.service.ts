import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {VacancyResponseDTO} from '../models/personalized/vacancy-response.model';

@Injectable({
    providedIn: 'root'
})
export class VacancyResponseService {
    private baseUrl = '/api/vacancies/my-responses';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<VacancyResponseDTO[]> {
        return this.http.get<VacancyResponseDTO[]>(this.baseUrl);
    }

    getById(id: string): Observable<VacancyResponseDTO> {
        return this.http.get<VacancyResponseDTO>(`${this.baseUrl}/${id}`);
    }

    create(response: VacancyResponseDTO): Observable<VacancyResponseDTO> {
        return this.http.post<VacancyResponseDTO>(this.baseUrl, response);
    }

    update(id: string, response: VacancyResponseDTO): Observable<VacancyResponseDTO> {
        return this.http.put<VacancyResponseDTO>(`${this.baseUrl}/${id}`, response);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}
