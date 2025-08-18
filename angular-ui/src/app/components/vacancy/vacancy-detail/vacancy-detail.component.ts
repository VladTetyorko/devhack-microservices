import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {VacancyService} from '../../../services/global/vacancy.service';
import {VacancyDTO} from '../../../models/global/vacancy.model';

@Component({
    selector: 'app-vacancy-detail',
    templateUrl: './vacancy-detail.component.html',
    styleUrls: ['./vacancy-detail.component.css']
})
export class VacancyDetailComponent implements OnInit {
    vacancy?: VacancyDTO;
    vacancyId!: string;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private vacancyService: VacancyService
    ) {
    }

    ngOnInit(): void {
        this.vacancyId = this.route.snapshot.paramMap.get('id')!;
        this.loadVacancy();
    }

    loadVacancy(): void {
        this.isLoading = true;
        this.vacancyService.getById(this.vacancyId).subscribe({
            next: (data) => {
                this.vacancy = {
                    ...data,
                    createdAt: this.convertDateFormat(data.createdAt),
                    updatedAt: this.convertDateFormat(data.updatedAt),
                    deadline: this.convertDateFormat(data.deadline),
                    openAt: this.convertDateFormat(data.openAt)
                };
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load vacancy details. ' + err.message;
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

    goBack(): void {
        this.router.navigate(['/vacancies']);
    }

    navigateToMyResponses(): void {
        this.router.navigate(['/vacancy-responses', 'my-responses']);
    }

    applyToVacancy(): void {
        // This would typically open a form or navigate to an application page
        // For now, we'll navigate to my responses
        this.router.navigate(['/vacancy-responses', 'my-responses']);
    }
}
