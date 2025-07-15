export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  token?: string;
  user?: any;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: any | null;
  token: string | null;
}
