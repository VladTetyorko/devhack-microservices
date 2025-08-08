import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user/user.service';
import {UserDTO} from '../../../models/user/user.model';

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
            const userData: UserDTO = {
                profile: {
                    name: this.registerForm.value.name
                },
                credentials: [{
                    provider: 'LOCAL',
                    email: this.registerForm.value.email
                }],
                access: {
                    role: this.isManagerRegistration ? 'MANAGER' : 'USER'
                }
            };

            const registerObservable = this.isManagerRegistration ?
                this.userService.registerManager(userData) :
                this.userService.register(userData);

            registerObservable.subscribe({
                next: (user) => {
                    this.isSubmitting = false;
                    this.router.navigate(['/users', user.id]);
                },
                error: (err) => {
                    this.error = 'Registration failed. ' + err.message;
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
