import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface UserAccessDTO extends BasisDtoEntityModel {
    role: string;
    aiUsageAllowed?: boolean;
    accountLocked?: boolean;
}
