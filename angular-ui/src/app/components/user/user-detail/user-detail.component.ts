import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../../services/user.service';
import {UserDTO} from '../../../models/user.model';

@Component({
    selector: 'app-user-detail',
    templateUrl: './user-detail.component.html',
    styleUrls: ['./user-detail.component.css']
})
export class UserDetailComponent implements OnInit {
    userForm!: FormGroup;
    userId!: string;
    isLoading = true;
    error = '';
    success = '';
    user?: UserDTO;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private userService: UserService
    ) {
    }

    ngOnInit(): void {
        this.userId = this.route.snapshot.paramMap.get('id')!;

        // Initialize the form
        this.userForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]]
        });

        this.loadUserDetails();
    }

    loadUserDetails(): void {
        this.isLoading = true;
        this.userService.getById(this.userId).subscribe({
            next: (userData) => {
                this.user = userData;
                this.userForm.patchValue({
                    name: userData.name,
                    email: userData.email
                });
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load user details. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    saveUser(): void {
        if (this.userForm.valid) {
            const updatedUser: UserDTO = {
                ...this.user,
                name: this.userForm.value.name,
                email: this.userForm.value.email
            };

            this.userService.update(this.userId, updatedUser).subscribe({
                next: () => {
                    this.success = 'User updated successfully!';
                    setTimeout(() => this.success = '', 3000);
                },
                error: (err) => {
                    this.error = 'Failed to update user. ' + err.message;
                }
            });
        } else {
            this.userForm.markAllAsTouched();
        }
    }

    deleteUser(): void {
        if (confirm('Are you sure you want to delete this user?')) {
            this.userService.delete(this.userId).subscribe({
                next: () => {
                    this.router.navigate(['/users']);
                },
                error: (err) => {
                    this.error = 'Failed to delete user. ' + err.message;
                }
            });
        }
    }

    goBack(): void {
        this.router.navigate(['/users']);
    }
}
