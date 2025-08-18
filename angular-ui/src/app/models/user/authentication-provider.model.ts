import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface AuthenticationProviderDTO extends BasisDtoEntityModel {
    provider: string;
    providerUserId?: string;
    email?: string;
    accessToken?: string;
    refreshToken?: string;
    tokenExpiry?: string;
}
