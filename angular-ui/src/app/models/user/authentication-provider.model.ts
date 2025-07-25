export interface AuthenticationProviderDTO {
  id?: string;
  provider: string;
  providerUserId?: string;
  email?: string;
  accessToken?: string;
  refreshToken?: string;
  tokenExpiry?: string;
}