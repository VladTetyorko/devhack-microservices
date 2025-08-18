import {UserDTO} from '../user/user.model';

export {UserDTO};

export interface LoginRequest {
    email: string;
    password: string;
    rememberMe?: boolean;
}

export interface LoginResponse {
    success: boolean;
    message: string;
    accessToken?: string;
    refreshToken?: string;
    tokenExpiry?: string;
    user?: UserDTO;
    roles?: string[];
}

export interface RegisterRequest {
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    acceptTerms: boolean;
}

export interface RegisterResponse {
    message: string;
    user?: UserDTO;
}

export interface PasswordResetRequest {
    email: string;
}

export interface PasswordResetConfirm {
    token: string;
    newPassword: string;
    confirmPassword: string;
}

export interface AuthState {
    isAuthenticated: boolean;
    user: UserDTO | null;
    roles: string[];
}
