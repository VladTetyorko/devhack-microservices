export interface UserAccessDTO {
  id?: string;
  role: string;
  aiUsageAllowed?: boolean;
  accountLocked?: boolean;
}