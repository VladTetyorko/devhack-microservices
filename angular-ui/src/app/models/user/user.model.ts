import {ProfileDTO} from './profile.model';
import {UserAccessDTO} from './user-access.model';
import {AuthenticationProviderDTO} from './authentication-provider.model';
import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface UserDTO extends BasisDtoEntityModel {
    credentials?: AuthenticationProviderDTO[];
    profile?: ProfileDTO;
    access?: UserAccessDTO;
}
