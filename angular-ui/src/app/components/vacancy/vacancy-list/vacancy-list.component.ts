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
    filteredVacancies: VacancyDTO[] = [];
    isLoading = true;
    error = '';

    // UI filters
    searchTerm = '';
    remoteOnly = false;

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
                this.applyFilters();
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load vacancies. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    onSearchChange(value: string): void {
        this.searchTerm = value;
        this.applyFilters();
    }

    onRemoteToggle(checked: boolean): void {
        this.remoteOnly = checked;
        this.applyFilters();
    }

    applyFilters(): void {
        let result = [...this.vacancies];
        if (this.searchTerm && this.searchTerm.trim().length > 0) {
            const q = this.searchTerm.toLowerCase();
            result = result.filter(v =>
                (v.companyName && v.companyName.toLowerCase().includes(q)) ||
                (v.position && v.position.toLowerCase().includes(q)) ||
                (v.technologies && v.technologies.toLowerCase().includes(q))
            );
        }
        if (this.remoteOnly) {
            result = result.filter(v => !!v.remoteAllowed);
        }
        this.filteredVacancies = result;
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

    editVacancy(id: string): void {
        this.router.navigate(['/vacancies', id, 'edit']);
    }

    navigateToMyResponses(): void {
        this.router.navigate(['/vacancy-responses', 'my-responses']);
    }
}
