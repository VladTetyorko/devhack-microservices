import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user.service';
import {UserDTO} from '../../../models/user.model';

@Component({
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
    users: UserDTO[] = [];
    isLoading = true;
    error = '';

    constructor(
        private userService: UserService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers(): void {
        this.isLoading = true;
        this.userService.getAll().subscribe({
            next: (data) => {
                this.users = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load users. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/users', id]);
    }

    createNew(): void {
        this.router.navigate(['/users/register']);
    }

    deleteUser(id: string, event: Event): void {
        event.stopPropagation();
        if (confirm('Are you sure you want to delete this user?')) {
            this.userService.delete(id).subscribe({
                next: () => {
                    this.loadUsers();
                },
                error: (err) => {
                    this.error = 'Failed to delete user. ' + err.message;
                }
            });
        }
    }
}
