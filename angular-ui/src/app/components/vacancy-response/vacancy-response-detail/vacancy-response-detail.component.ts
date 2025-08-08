// vacancy-response-detail.component.ts
import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Subscription} from 'rxjs';
import {VacancyResponseService} from '../../../services/personalized/vacancy-response.service';
import {VacancyResponseDTO} from '../../../models/personalized/vacancy-response.model';

@Component({
    selector: 'app-vacancy-response-detail',
    templateUrl: './vacancy-response-detail.component.html',
    styleUrls: ['./vacancy-response-detail.component.css']
})
export class VacancyResponseDetailComponent implements OnInit, OnDestroy {
    responses: VacancyResponseDTO[] = [];
    response?: VacancyResponseDTO;
    responseId!: string;
    isLoading = true;
    error = '';

    private routeSub!: Subscription;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private vacancyResponseService: VacancyResponseService
    ) {
    }

    ngOnInit(): void {
        // 1. Load the list
        this.vacancyResponseService.getAll().subscribe({
            next: list => {
                this.responses = list;
            },
            error: err => {
                this.error = 'Failed to load list. ' + err.message;
            }
        });

        // 2. Watch for changes to the :id param
        this.routeSub = this.route.paramMap.subscribe((params: ParamMap) => {
            const id = params.get('id');
            if (id) {
                this.responseId = id;
                this.loadResponseDetails(id);
            }
        });
    }

    ngOnDestroy(): void {
        this.routeSub?.unsubscribe();
    }

    private loadResponseDetails(id: string): void {
        this.isLoading = true;
        this.error = '';
        this.vacancyResponseService.getById(id).subscribe({
            next: data => {
                this.response = {
                    ...data,
                    createdAt: this.convertDateFormat(data.createdAt),
                    updatedAt: this.convertDateFormat(data.updatedAt)
                };
                this.isLoading = false;
            },
            error: err => {
                this.error = 'Failed to load details. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    private convertDateFormat(dateString?: string): string | undefined {
        if (!dateString) return dateString;
        if (dateString.includes(',')) {
            try {
                const parts = dateString.split(',').map(p => parseInt(p, 10));
                const [y, m, d, hh, mm, ss] = parts;
                return new Date(y, m - 1, d, hh, mm, ss).toISOString();
            } catch {
            }
        }
        return dateString;
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

    goToVacancy(vacancyId?: string): void {
        if (vacancyId) {
            this.router.navigateByUrl(`/vacancies/${vacancyId}`);
        }
    }


    goBack(): void {
        this.router.navigate(['/vacancy-responses']);
    }
}
