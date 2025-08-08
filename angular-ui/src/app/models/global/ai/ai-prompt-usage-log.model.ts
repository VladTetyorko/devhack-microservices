import {UserOwnedEntity} from '../../user-owned-entity.model';

export interface AiPromptUsageLogModel extends UserOwnedEntity {
    promptId: string;
    promptCode?: string;
    input?: string;
    result?: string;
}