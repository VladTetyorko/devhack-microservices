import {UserOwnedEntity} from "../user-owned-entity.model";

export interface NoteDTO extends UserOwnedEntity {
    questionId: string;
    questionText: string;
    noteText?: string;
}
