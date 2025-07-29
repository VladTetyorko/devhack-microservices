import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AiPromptCategoryService} from '../../../services/ai-prompt-category.service';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

@Component({
    selector: 'app-ai-prompt-category-list',
    templateUrl: './ai-prompt-category-list.component.html',
    styleUrls: ['./ai-prompt-category-list.component.css']
})
export class AiPromptCategoryListComponent implements OnInit {
    categories: AiPromptCategoryModel[] = [];
    isLoading = true;
    error = '';

    constructor(
        private aiPromptCategoryService: AiPromptCategoryService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadCategories();
    }

    loadCategories(): void {
        this.isLoading = true;
        this.aiPromptCategoryService.getAll().subscribe({
            next: (data) => {
                this.categories = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt categories. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/ai-prompt-categories', id]);
    }

    loadWithPrompts(): void {
        this.isLoading = true;
        this.aiPromptCategoryService.getWithPrompts().subscribe({
            next: (data) => {
                this.categories = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt categories with prompts. ' + err.message;
                this.isLoading = false;
            }
        });
    }
}