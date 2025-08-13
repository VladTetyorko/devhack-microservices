import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

@Component({
    selector: 'app-category-list',
    templateUrl: './category-list.component.html',
    styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
    categories: TagDTO[] = [];
    isLoading = false;
    error = '';
    successMessage = '';
    searchTerm = '';

    constructor(
        private tagService: TagService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadCategories();
    }

    loadCategories(): void {
        this.isLoading = true;
        this.error = '';

        this.tagService.getRootTags().subscribe({
            next: (categories) => {
                this.categories = categories;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load categories. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    get filteredCategories(): TagDTO[] {
        if (!this.searchTerm) {
            return this.categories;
        }
        return this.categories.filter(category =>
            category.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
            (category.description && category.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
        );
    }

    clearSearch(): void {
        this.searchTerm = '';
    }

    viewDetail(id: string): void {
        this.router.navigate(['/categories', id]);
    }

    editCategory(id: string): void {
        this.router.navigate(['/categories', id, 'edit']);
    }

    createNew(): void {
        this.router.navigate(['/categories/create']);
    }

    deleteCategory(id: string, event: Event): void {
        event.stopPropagation();

        if (confirm(`Are you sure you want to delete this category? This will also delete all topics within this category. This action cannot be undone.`)) {
            this.tagService.deleteWithCascade(id, true).subscribe({
                next: () => {
                    this.successMessage = `Category has been successfully deleted.`;
                    this.loadCategories(); // Reload the list
                    // Clear success message after 5 seconds
                    setTimeout(() => this.successMessage = '', 5000);
                },
                error: (err) => {
                    this.error = 'Failed to delete category. ' + err.message;
                }
            });
        }
    }

    viewTopics(categoryId: string): void {
        this.router.navigate(['/topics'], {queryParams: {category: categoryId}});
    }
}