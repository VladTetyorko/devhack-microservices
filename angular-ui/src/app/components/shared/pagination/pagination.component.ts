import {Component, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {Page} from '../../../models/basic/page.model';

@Component({
    selector: 'app-pagination',
    templateUrl: './pagination.component.html',
    styleUrls: ['./pagination.component.css']
})
export class PaginationComponent implements OnChanges {
    @Input() page!: Page<any>;
    @Input() maxVisiblePages: number = 5;
    @Output() pageChange = new EventEmitter<number>();
    @Output() sizeChange = new EventEmitter<number>();

    visiblePages: number[] = [];
    pageSizeOptions = [5, 10, 20, 50];

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['page'] && this.page) {
            this.calculateVisiblePages();
        }
    }

    private calculateVisiblePages(): void {
        const totalPages = this.page.totalPages;
        const currentPage = this.page.number;
        const maxVisible = this.maxVisiblePages;

        let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);

        // Adjust start page if we're near the end
        if (endPage - startPage + 1 < maxVisible) {
            startPage = Math.max(0, endPage - maxVisible + 1);
        }

        this.visiblePages = [];
        for (let i = startPage; i <= endPage; i++) {
            this.visiblePages.push(i);
        }
    }

    onPageClick(pageNumber: number): void {
        if (pageNumber !== this.page.number && pageNumber >= 0 && pageNumber < this.page.totalPages) {
            this.pageChange.emit(pageNumber);
        }
    }

    onSizeChange(size: number): void {
        this.sizeChange.emit(size);
    }

    goToFirstPage(): void {
        if (!this.page.first) {
            this.pageChange.emit(0);
        }
    }

    goToPreviousPage(): void {
        if (!this.page.first) {
            this.pageChange.emit(this.page.number - 1);
        }
    }

    goToNextPage(): void {
        if (!this.page.last) {
            this.pageChange.emit(this.page.number + 1);
        }
    }

    goToLastPage(): void {
        if (!this.page.last) {
            this.pageChange.emit(this.page.totalPages - 1);
        }
    }

    getDisplayedRange(): string {
        const start = this.page.number * this.page.size + 1;
        const end = Math.min(start + this.page.numberOfElements - 1, this.page.totalElements);
        return `${start}-${end}`;
    }
}
