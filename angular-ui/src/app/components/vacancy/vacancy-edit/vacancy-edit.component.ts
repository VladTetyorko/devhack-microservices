import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Subscription} from 'rxjs';
import {VacancyDTO} from '../../../models/global/vacancy.model';
import {VacancyService} from '../../../services/global/vacancy.service';

@Component({
    selector: 'app-vacancy-edit',
    templateUrl: './vacancy-edit.component.html',
    styleUrls: ['./vacancy-edit.component.css']
})
export class VacancyEditComponent implements OnInit, OnDestroy {
    form!: FormGroup;
    vacancyId!: string;
    isLoading = true;
    isSaving = false;
    error = '';
    private subs: Subscription[] = [];

    statusOptions = [
        {value: 'OPEN', label: 'Open'},
        {value: 'CLOSED', label: 'Closed'},
        {value: 'EXPIRED', label: 'Expired'}
    ];

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private vacancyService: VacancyService,
    ) {
    }

    ngOnInit(): void {
        this.vacancyId = this.route.snapshot.paramMap.get('id')!;
        this.initForm();
        this.loadVacancy();
    }

    private initForm(): void {
        this.form = this.fb.group({
            companyName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            position: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            technologies: [''],
            source: [''],
            url: [''],
            status: ['OPEN', [Validators.required]],
            contactPerson: [''],
            contactEmail: ['', [Validators.email]],
            deadline: [''],
            remoteAllowed: [false],
            description: ['']
        });
    }

    private loadVacancy(): void {
        this.isLoading = true;
        const s = this.vacancyService.getById(this.vacancyId).subscribe({
            next: (v: VacancyDTO) => {
                // Normalize dates that may arrive as comma-separated strings
                const normalize = (d?: string) => {
                    if (!d) return d;
                    if (d.includes(',')) {
                        try {
                            const p = d.split(',').map(x => parseInt(x, 10));
                            if (p.length >= 6) {
                                const dt = new Date(p[0], p[1] - 1, p[2], p[3], p[4], p[5]);
                                return dt.toISOString();
                            }
                        } catch {
                        }
                    }
                    return d;
                };

                this.form.patchValue({
                    companyName: v.companyName,
                    position: v.position,
                    technologies: v.technologies,
                    source: v.source,
                    url: v.url,
                    status: (v.status as any) ?? 'OPEN',
                    contactPerson: v.contactPerson,
                    contactEmail: v.contactEmail,
                    deadline: normalize(v.deadline),
                    remoteAllowed: v.remoteAllowed ?? false,
                    description: v.description
                });
                this.isLoading = false;
            },
            error: err => {
                this.error = 'Failed to load vacancy. ' + err.message;
                this.isLoading = false;
            }
        });
        this.subs.push(s);
    }

    get f() {
        return this.form.controls;
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.isSaving = true;
        const dto: VacancyDTO = {
            id: this.vacancyId,
            ...this.form.value,
        } as VacancyDTO;

        const s = this.vacancyService.update(this.vacancyId, dto).subscribe({
            next: () => {
                this.isSaving = false;
                this.router.navigate(['/vacancies', this.vacancyId]);
            },
            error: err => {
                this.error = 'Failed to save vacancy. ' + err.message;
                this.isSaving = false;
            }
        });
        this.subs.push(s);
    }

    cancel(): void {
        this.router.navigate(['/vacancies', this.vacancyId]);
    }

    ngOnDestroy(): void {
        this.subs.forEach(s => s.unsubscribe());
    }
}
