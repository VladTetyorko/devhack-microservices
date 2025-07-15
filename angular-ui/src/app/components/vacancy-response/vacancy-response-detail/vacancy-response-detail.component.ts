import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {VacancyResponseService} from '../../../services/vacancy-response.service';
import {VacancyResponseDTO} from '../../../models/vacancy-response.model';

@Component({
    selector: 'app-vacancy-response-detail',
    templateUrl: './vacancy-response-detail.component.html',
    styleUrls: ['./vacancy-response-detail.component.css']
})
export class VacancyResponseDetailComponent implements OnInit {
    response?: VacancyResponseDTO;
    responseId!: string;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private vacancyResponseService: VacancyResponseService
    ) {
    }

    ngOnInit(): void {
        this.responseId = this.route.snapshot.paramMap.get('id')!;
        this.loadResponseDetails();
    }

    loadResponseDetails(): void {
        this.isLoading = true;
        this.vacancyResponseService.getById(this.responseId).subscribe({
            next: (data) => {
                this.response = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load response details. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'ACCEPTED':
                return 'bg-success';
            case 'REJECTED':
                return 'bg-danger';
            case 'PENDING':
                return 'bg-warning text-dark';
            default:
                return 'bg-secondary';
        }
    }

    goBack(): void {
        this.router.navigate(['/vacancies/my-responses']);
    }
}
