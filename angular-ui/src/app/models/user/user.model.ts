import {ProfileDTO} from './profile.model';
import {UserAccessDTO} from './user-access.model';
import {AuthenticationProviderDTO} from './authentication-provider.model';

export interface UserDTO {
    id?: string;
    createdAt?: string;
    credentials?: AuthenticationProviderDTO[];
    profile?: ProfileDTO;
    access?: UserAccessDTO;
}

