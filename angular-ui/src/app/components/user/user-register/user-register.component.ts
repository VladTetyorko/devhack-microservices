import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {UserRegistrationRequest, UserService} from '../../../services/user/user.service';

@Component({
    selector: 'app-user-register',
    templateUrl: './user-register.component.html',
    styleUrls: ['./user-register.component.css']
})
export class UserRegisterComponent implements OnInit {
    registerForm!: FormGroup;
    isSubmitting = false;
    error = '';
    isManagerRegistration = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private userService: UserService
    ) {
    }

    ngOnInit(): void {
        this.registerForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]]
        });
    }

    register(): void {
        if (this.registerForm.valid) {
            this.isSubmitting = true;
            const registrationData: UserRegistrationRequest = {
                name: this.registerForm.value.name,
                email: this.registerForm.value.email,
                role: this.isManagerRegistration ? 'MANAGER' : 'USER'
            };

            const registerObservable = this.isManagerRegistration ?
                this.userService.registerManager(registrationData) :
                this.userService.register(registrationData);

            registerObservable.subscribe({
                next: (user) => {
                    this.isSubmitting = false;
                    this.router.navigate(['/users', user.id]);
                },
                error: (err) => {
                    this.error = 'Registration failed. ' + (err.error?.message || err.message || 'Unknown error');
                    this.isSubmitting = false;
                }
            });
        } else {
            this.registerForm.markAllAsTouched();
        }
    }

    toggleRegistrationType(): void {
        this.isManagerRegistration = !this.isManagerRegistration;
    }

    cancel(): void {
        this.router.navigate(['/users']);
    }
}
