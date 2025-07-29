import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptCategoryService} from '../../../services/ai-prompt-category.service';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

@Component({
    selector: 'app-ai-prompt-category-detail',
    templateUrl: './ai-prompt-category-detail.component.html',
    styleUrls: ['./ai-prompt-category-detail.component.css']
})
export class AiPromptCategoryDetailComponent implements OnInit {
    category: AiPromptCategoryModel | null = null;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptCategoryService: AiPromptCategoryService
    ) {
    }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadCategory(id);
        } else {
            this.error = 'No category ID provided';
            this.isLoading = false;
        }
    }

    loadCategory(id: string): void {
        this.isLoading = true;
        this.aiPromptCategoryService.getById(id).subscribe({
            next: (data) => {
                this.category = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt category. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/ai-prompt-categories']);
    }

    viewPrompt(promptId: string): void {
        this.router.navigate(['/ai-prompts', promptId]);
    }
}