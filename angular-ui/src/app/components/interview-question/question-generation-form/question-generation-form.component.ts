import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {GenerateQuestionsRequest, InterviewQuestionService} from '../../../services/global/interview-question.service';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

/**
 * Component for generating interview questions using AI.
 * Provides an interactive form with comprehensive options for question generation.
 */
@Component({
    selector: 'app-question-generation-form',
    templateUrl: './question-generation-form.component.html',
    styleUrls: ['./question-generation-form.component.css']
})
export class QuestionGenerationFormComponent implements OnInit, OnDestroy {
    generationForm: FormGroup;
    tags: TagDTO[] = [];
    loading = false;
    generating = false;
    error: string | null = null;
    successMessage: string | null = null;
    isAiModuleError = false;
    showAiSetupInstructions = false;
    aiProcessingMessage = 'Initializing AI request...';
    aiProcessingProgress = 0;
    private aiProgressInterval: any;

    // Form options
    difficultyOptions = [
        {value: 'Easy', label: 'Easy', description: 'Basic concepts and simple questions'},
        {value: 'Medium', label: 'Medium', description: 'Intermediate level with moderate complexity'},
        {value: 'Hard', label: 'Hard', description: 'Advanced concepts and challenging questions'}
    ];

    typeOptions = [
        {value: 'mixed', label: 'Mixed', description: 'Combination of different question types'},
        {value: 'theoretical', label: 'Theoretical', description: 'Concept-based questions'},
        {value: 'practical', label: 'Practical', description: 'Hands-on coding questions'},
        {value: 'behavioral', label: 'Behavioral', description: 'Soft skills and experience questions'}
    ];

    experienceOptions = [
        {value: 'junior', label: 'Junior (0-2 years)', description: 'Entry-level positions'},
        {value: 'mid', label: 'Mid-level (2-5 years)', description: 'Intermediate experience'},
        {value: 'senior', label: 'Senior (5+ years)', description: 'Advanced experience'}
    ];

    // Quick generation presets
    quickPresets = [
        {name: 'Quick Start', topic: 'JavaScript', count: 3, difficulty: 'Easy', type: 'mixed', experience: 'junior'},
        {name: 'Interview Prep', topic: 'Java', count: 5, difficulty: 'Medium', type: 'mixed', experience: 'mid'},
        {
            name: 'Advanced Challenge',
            topic: 'System Design',
            count: 3,
            difficulty: 'Hard',
            type: 'theoretical',
            experience: 'senior'
        }
    ];

    constructor(
        private fb: FormBuilder,
        private questionService: InterviewQuestionService,
        private tagService: TagService,
        private router: Router
    ) {
        this.generationForm = this.createForm();
    }

    ngOnInit(): void {
        this.loadTags();
    }

    /**
     * Create the reactive form with validation
     */
    private createForm(): FormGroup {
        return this.fb.group({
            topic: ['', [Validators.required, Validators.minLength(2)]],
            count: [5, [Validators.required, Validators.min(1), Validators.max(20)]],
            difficulty: ['Medium', Validators.required],
            type: ['mixed', Validators.required],
            experience: ['junior', Validators.required]
        });
    }

    /**
     * Load available tags for topic selection
     */
    loadTags(): void {
        this.loading = true;
        this.tagService.getAll().subscribe({
            next: (tags: TagDTO[]) => {
                this.tags = tags;
                this.loading = false;
            },
            error: (error: any) => {
                console.error('Error loading tags:', error);
                this.error = 'Failed to load available topics. You can still enter a custom topic.';
                this.loading = false;
            }
        });
    }

    /**
     * Handle form submission for question generation
     */
    onSubmit(): void {
        if (this.generationForm.valid && !this.generating) {
            this.generateQuestions();
        } else {
            this.markFormGroupTouched();
        }
    }

    /**
     * Generate questions using the form data
     */
    private generateQuestions(): void {
        this.generating = true;
        this.error = null;
        this.successMessage = null;
        this.isAiModuleError = false;
        this.showAiSetupInstructions = false;

        const formValue = this.generationForm.value;
        const request: GenerateQuestionsRequest = {
            topic: formValue.topic,
            count: formValue.count,
            difficulty: formValue.difficulty,
            type: formValue.type,
            experience: formValue.experience
        };

        // Start AI processing simulation
        this.startAiProcessingSimulation();

        // Set timeout for AI processing (90 seconds)
        const timeoutId = setTimeout(() => {
            if (this.generating) {
                this.stopAiProcessingSimulation();
                this.generating = false;
                this.error = 'AI processing timeout. The request is taking longer than expected.';
                this.isAiModuleError = true;
            }
        }, 90000);

        this.questionService.generateQuestions(request).subscribe({
            next: (response) => {
                clearTimeout(timeoutId);
                this.stopAiProcessingSimulation();
                this.generating = false;
                if (response.success) {
                    this.successMessage = response.message;
                    this.showSuccessAndRedirect();
                } else {
                    this.error = response.message || 'Generation failed. Please try again.';
                    this.detectAiModuleError(response.message);
                }
            },
            error: (error) => {
                clearTimeout(timeoutId);
                this.stopAiProcessingSimulation();
                console.error('Error generating questions:', error);
                this.generating = false;
                const errorMessage = error.error?.message || 'Failed to generate questions. Please try again.';
                this.error = errorMessage;
                this.detectAiModuleError(errorMessage);
            }
        });
    }

    /**
     * Apply a quick preset to the form
     */
    applyPreset(preset: any): void {
        this.generationForm.patchValue({
            topic: preset.topic,
            count: preset.count,
            difficulty: preset.difficulty,
            type: preset.type,
            experience: preset.experience
        });
    }

    /**
     * Auto-generate easy questions for a selected tag
     */
    autoGenerateEasy(tagName: string): void {
        this.generating = true;
        this.error = null;
        this.successMessage = null;
        this.isAiModuleError = false;
        this.showAiSetupInstructions = false;

        // Start AI processing simulation
        this.startAiProcessingSimulation();

        // Set timeout for AI processing (90 seconds)
        const timeoutId = setTimeout(() => {
            if (this.generating) {
                this.stopAiProcessingSimulation();
                this.generating = false;
                this.error = 'AI processing timeout. The request is taking longer than expected.';
                this.isAiModuleError = true;
            }
        }, 90000);

        this.questionService.autoGenerateEasyQuestions(tagName).subscribe({
            next: (response) => {
                clearTimeout(timeoutId);
                this.stopAiProcessingSimulation();
                this.generating = false;
                if (response.success) {
                    this.successMessage = response.message;
                    this.showSuccessAndRedirect();
                } else {
                    this.error = response.message || 'Auto-generation failed. Please try again.';
                    this.detectAiModuleError(response.message);
                }
            },
            error: (error) => {
                clearTimeout(timeoutId);
                this.stopAiProcessingSimulation();
                console.error('Error auto-generating questions:', error);
                this.generating = false;
                const errorMessage = error.error?.message || 'Failed to auto-generate questions. Please try again.';
                this.error = errorMessage;
                this.detectAiModuleError(errorMessage);
            }
        });
    }

    /**
     * Show success message and redirect after delay
     */
    private showSuccessAndRedirect(): void {
        setTimeout(() => {
            this.router.navigate(['/questions']);
        }, 3000);
    }

    /**
     * Mark all form fields as touched to show validation errors
     */
    private markFormGroupTouched(): void {
        Object.keys(this.generationForm.controls).forEach(key => {
            const control = this.generationForm.get(key);
            control?.markAsTouched();
        });
    }

    /**
     * Check if a form field has an error
     */
    hasError(fieldName: string, errorType: string): boolean {
        const field = this.generationForm.get(fieldName);
        return !!(field && field.hasError(errorType) && field.touched);
    }

    /**
     * Get error message for a form field
     */
    getErrorMessage(fieldName: string): string {
        const field = this.generationForm.get(fieldName);
        if (!field || !field.errors || !field.touched) return '';

        if (field.hasError('required')) return `${fieldName} is required`;
        if (field.hasError('minlength')) return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
        if (field.hasError('min')) return `${fieldName} must be at least ${field.errors['min'].min}`;
        if (field.hasError('max')) return `${fieldName} must be at most ${field.errors['max'].max}`;

        return 'Invalid value';
    }

    /**
     * Reset the form to default values
     */
    resetForm(): void {
        this.generationForm.reset({
            topic: '',
            count: 5,
            difficulty: 'Medium',
            type: 'mixed',
            experience: 'junior'
        });
        this.error = null;
        this.successMessage = null;
    }

    /**
     * Navigate to statistics page
     */
    viewStats(): void {
        this.router.navigate(['/questions/stats']);
    }

    /**
     * Start AI processing simulation with progress updates
     */
    private startAiProcessingSimulation(): void {
        this.aiProcessingProgress = 0;
        this.aiProcessingMessage = 'Sending request to AI module...';

        const messages = [
            'Sending request to AI module...',
            'AI is analyzing the topic...',
            'Generating question ideas...',
            'Refining question quality...',
            'Finalizing questions...',
            'Almost done...'
        ];

        let messageIndex = 0;
        let progress = 0;

        this.aiProgressInterval = setInterval(() => {
            if (progress < 95) {
                progress += Math.random() * 15 + 5; // Random progress between 5-20%
                if (progress > 95) progress = 95;

                this.aiProcessingProgress = progress;

                // Update message based on progress
                const targetMessageIndex = Math.floor((progress / 100) * messages.length);
                if (targetMessageIndex > messageIndex && targetMessageIndex < messages.length) {
                    messageIndex = targetMessageIndex;
                    this.aiProcessingMessage = messages[messageIndex];
                }
            }
        }, 2000); // Update every 2 seconds
    }

    /**
     * Stop AI processing simulation
     */
    private stopAiProcessingSimulation(): void {
        if (this.aiProgressInterval) {
            clearInterval(this.aiProgressInterval);
            this.aiProgressInterval = null;
        }
        this.aiProcessingProgress = 100;
        this.aiProcessingMessage = 'Processing complete!';
    }

    /**
     * Detect if error is related to AI module not being available
     */
    private detectAiModuleError(errorMessage: string | null): void {
        if (!errorMessage) return;

        const aiModuleErrorIndicators = [
            'timeout',
            'connection refused',
            'service unavailable',
            'kafka',
            'no response',
            'processing failed',
            'internal error',
            'failed to process'
        ];

        const lowerErrorMessage = errorMessage.toLowerCase();
        this.isAiModuleError = aiModuleErrorIndicators.some(indicator =>
            lowerErrorMessage.includes(indicator)
        );
    }

    /**
     * Clean up intervals on component destroy
     */
    ngOnDestroy(): void {
        this.stopAiProcessingSimulation();
    }
}