import {BasisDtoEntityModel} from "./basis-dto-entity.model";

export interface UserOwnedEntity extends BasisDtoEntityModel {
    userId?: string;
    userName?: string;
}