import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AiPromptService} from '../../../services/ai-prompt.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';

@Component({
    selector: 'app-ai-prompt-list',
    templateUrl: './ai-prompt-list.component.html',
    styleUrls: ['./ai-prompt-list.component.css']
})
export class AiPromptListComponent implements OnInit {
    prompts: AiPromptModel[] = [];
    isLoading = true;
    error = '';

    constructor(
        private aiPromptService: AiPromptService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadPrompts();
    }

    loadPrompts(): void {
        this.isLoading = true;
        this.aiPromptService.getAll().subscribe({
            next: (data) => {
                this.prompts = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompts. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/ai-prompts', id]);
    }

    toggleActive(prompt: AiPromptModel): void {
        if (prompt.active) {
            this.aiPromptService.deactivate(prompt.id!).subscribe({
                next: () => this.loadPrompts(),
                error: (err) => this.error = 'Failed to deactivate prompt. ' + err.message
            });
        } else {
            this.aiPromptService.activate(prompt.id!).subscribe({
                next: () => this.loadPrompts(),
                error: (err) => this.error = 'Failed to activate prompt. ' + err.message
            });
        }
    }
}