import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {VacancyService} from '../../../services/global/vacancy.service';
import {VacancyDTO} from '../../../models/global/vacancy.model';

@Component({
    selector: 'app-vacancy-list',
    templateUrl: './vacancy-list.component.html',
    styleUrls: ['./vacancy-list.component.css']
})
export class VacancyListComponent implements OnInit {
    vacancies: VacancyDTO[] = [];
    isLoading = true;
    error = '';

    constructor(
        private vacancyService: VacancyService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadVacancies();
    }

    loadVacancies(): void {
        this.isLoading = true;
        this.vacancyService.getAll().subscribe({
            next: (data) => {
                this.vacancies = data.map(vacancy => ({
                    ...vacancy,
                    createdAt: this.convertDateFormat(vacancy.createdAt),
                    updatedAt: this.convertDateFormat(vacancy.updatedAt)
                }));
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load vacancies. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    private convertDateFormat(dateString?: string): string | undefined {
        if (!dateString) return dateString;

        // Check if the date is in comma-separated format (e.g., "2025,7,7,15,36,51,426621000")
        if (dateString.includes(',')) {
            try {
                const parts = dateString.split(',').map(part => parseInt(part, 10));
                if (parts.length >= 6) {
                    // parts: [year, month, day, hour, minute, second, nanoseconds]
                    // Note: month is 1-based in the input, but Date constructor expects 0-based
                    const date = new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
                    return date.toISOString();
                }
            } catch (error) {
                console.warn('Failed to parse date:', dateString, error);
            }
        }

        return dateString;
    }

    viewDetail(id: string): void {
        this.router.navigate(['/vacancies', id]);
    }

    navigateToMyResponses(): void {
        this.router.navigate(['/vacancies/my-responses']);
    }
}
