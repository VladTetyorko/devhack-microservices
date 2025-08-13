import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface UserDTO extends BasisDtoEntityModel {
    credentialIds?: string[];
    profileId?: string;
    accessId?: string;
}
