import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {VacancyResponseService} from '../../../services/personalized/vacancy-response.service';
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
            next: (data: VacancyResponseDTO[]) => {
                this.responses = data.map((response: VacancyResponseDTO) => ({
                    ...response,
                    createdAt: this.convertDateFormat(response.createdAt),
                    updatedAt: this.convertDateFormat(response.updatedAt)
                }));
                this.isLoading = false;
            },
            error: (err: any) => {
                this.error = 'Failed to load vacancy responses. ' + err.message;
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
        this.router.navigate(['/vacancy-responses', id]);
    }
}
