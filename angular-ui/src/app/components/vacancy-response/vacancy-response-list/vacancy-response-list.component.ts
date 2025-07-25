import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {VacancyResponseService} from '../../../services/vacancy-response.service';
import {VacancyResponseDTO} from '../../../models/personalized/vacancy-response.model';

@Component({
    selector: 'app-vacancy-response-list',
    templateUrl: './vacancy-response-list.component.html',
    styleUrls: ['./vacancy-response-list.component.css']
})
export class VacancyResponseListComponent implements OnInit {
    responses: VacancyResponseDTO[] = [];
    isLoading = true;
    error = '';

    constructor(
        private vacancyResponseService: VacancyResponseService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadResponses();
    }

    loadResponses(): void {
        this.isLoading = true;
        this.vacancyResponseService.getAll().subscribe({
            next: (data) => {
                this.responses = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load vacancy responses. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/vacancies/my-responses', id]);
    }
}
