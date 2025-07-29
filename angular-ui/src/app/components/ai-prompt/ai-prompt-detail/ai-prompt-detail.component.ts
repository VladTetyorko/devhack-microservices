import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptService} from '../../../services/ai-prompt.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';

@Component({
    selector: 'app-ai-prompt-detail',
    templateUrl: './ai-prompt-detail.component.html',
    styleUrls: ['./ai-prompt-detail.component.css']
})
export class AiPromptDetailComponent implements OnInit {
    prompt: AiPromptModel | null = null;
    isLoading = true;
    error = '';
    isEditing = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptService: AiPromptService
    ) {
    }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadPrompt(id);
        } else {
            this.error = 'No prompt ID provided';
            this.isLoading = false;
        }
    }

    loadPrompt(id: string): void {
        this.isLoading = true;
        this.aiPromptService.getById(id).subscribe({
            next: (data) => {
                this.prompt = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    toggleActive(): void {
        if (!this.prompt) return;

        if (this.prompt.active) {
            this.aiPromptService.deactivate(this.prompt.id!).subscribe({
                next: (updatedPrompt) => {
                    this.prompt = updatedPrompt;
                },
                error: (err) => this.error = 'Failed to deactivate prompt. ' + err.message
            });
        } else {
            this.aiPromptService.activate(this.prompt.id!).subscribe({
                next: (updatedPrompt) => {
                    this.prompt = updatedPrompt;
                },
                error: (err) => this.error = 'Failed to activate prompt. ' + err.message
            });
        }
    }

    goBack(): void {
        this.router.navigate(['/ai-prompts']);
    }
}