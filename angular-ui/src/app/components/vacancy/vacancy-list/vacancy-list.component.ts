import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {VacancyService} from '../../../services/global/vacancy.service';
import {VacancyDTO} from '../../../models/global/vacancy.model';
import {Page} from '../../../models/basic/page.model';

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

    // Paging state
    page = 0;
    size = 12;
    sort: string[] = ['createdAt,desc'];
    pageData?: Page<VacancyDTO>;

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
        this.vacancyService.getAllPaged({page: this.page, size: this.size, sort: this.sort}).subscribe({
            next: (resp: Page<VacancyDTO>) => {
                this.pageData = resp;
                this.vacancies = resp.content.map(vacancy => ({
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

    goToPage(page: number): void {
        if (!this.pageData) return;
        if (page < 0 || page >= this.pageData.totalPages || page === this.page) return;
        this.page = page;
        this.loadVacancies();
    }

    onPageSizeChange(newSize: number): void {
        if (newSize > 0 && newSize !== this.size) {
            this.size = newSize;
            this.page = 0;
            this.loadVacancies();
        }
    }

    onSearchChange(value: string): void {
        this.searchTerm = value;
        const trimmed = (value || '').trim();
        if (trimmed.length === 0) {
            // Reset to paged list
            this.page = 0;
            this.loadVacancies();
            return;
        }
        this.isLoading = true;
        this.vacancyService.search({keyword: trimmed}).subscribe({
            next: (data) => {
                // When searching, we display all returned results without pagination for simplicity
                this.pageData = undefined;
                this.vacancies = data.map(vacancy => ({
                    ...vacancy,
                    createdAt: this.convertDateFormat(vacancy.createdAt),
                    updatedAt: this.convertDateFormat(vacancy.updatedAt)
                }));
                this.applyFilters();
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to search vacancies. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    onRemoteToggle(checked: boolean): void {
        this.remoteOnly = checked;
        // We keep remote filtering client-side to allow combining with search results
        this.applyFilters();
    }

    applyFilters(): void {
        let result = [...this.vacancies];
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
