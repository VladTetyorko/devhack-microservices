import {Component, EventEmitter, Input, Output} from '@angular/core';

export interface ActionButton {
    id: string;
    label: string;
    icon: string;
    cssClass?: string;
    action: string;
    visible?: boolean;
}

export interface ActionButtonEvent {
    action: string;
    entityId: string | number;
    event?: MouseEvent;
}

@Component({
    selector: 'app-action-buttons',
    templateUrl: './action-buttons.component.html',
    styleUrls: ['./action-buttons.component.scss']
})
export class ActionButtonsComponent {
    @Input() entityId!: string | number;
    @Input() variant: 'buttons' | 'dropdown' = 'buttons';
    @Input() size: 'sm' | 'md' | 'lg' = 'sm';

    // Standard button visibility controls (backward compatibility)
    @Input() showView: boolean = true;
    @Input() showEdit: boolean = true;
    @Input() showDelete: boolean = true;

    // Custom actions support
    @Input() customActions: ActionButton[] = [];
    @Input() dropdownToggleClass: string = 'btn btn-sm btn-outline-secondary dropdown-toggle';

    @Output() view = new EventEmitter<string | number>();
    @Output() edit = new EventEmitter<string | number>();
    @Output() delete = new EventEmitter<{ id: string | number, event: MouseEvent }>();
    @Output() customAction = new EventEmitter<ActionButtonEvent>();

    get allActions(): ActionButton[] {
        const standardActions: ActionButton[] = [];

        if (this.showView) {
            standardActions.push({
                id: 'view',
                label: 'View Details',
                icon: 'bi-eye',
                cssClass: this.variant === 'dropdown' ? 'dropdown-item' : 'btn btn-outline-info',
                action: 'view',
                visible: true
            });
        }

        if (this.showEdit) {
            standardActions.push({
                id: 'edit',
                label: 'Edit',
                icon: 'bi-pencil',
                cssClass: this.variant === 'dropdown' ? 'dropdown-item' : 'btn btn-outline-primary',
                action: 'edit',
                visible: true
            });
        }

        // Add custom actions
        const visibleCustomActions = this.customActions.filter(action => action.visible !== false);
        standardActions.push(...visibleCustomActions);

        if (this.showDelete) {
            standardActions.push({
                id: 'delete',
                label: 'Delete',
                icon: 'bi-trash',
                cssClass: this.variant === 'dropdown' ? 'dropdown-item text-danger' : 'btn btn-outline-danger',
                action: 'delete',
                visible: true
            });
        }

        return standardActions;
    }

    get buttonSizeClass(): string {
        return this.size === 'sm' ? 'btn-sm' : this.size === 'lg' ? 'btn-lg' : '';
    }

    onAction(action: ActionButton, event?: MouseEvent) {
        if (event) {
            event.stopPropagation();
        }

        switch (action.action) {
            case 'view':
                this.view.emit(this.entityId);
                break;
            case 'edit':
                this.edit.emit(this.entityId);
                break;
            case 'delete':
                this.delete.emit({id: this.entityId, event: event!});
                break;
            default:
                this.customAction.emit({
                    action: action.action,
                    entityId: this.entityId,
                    event: event
                });
                break;
        }
    }

    // Backward compatibility methods
    onView() {
        this.view.emit(this.entityId);
    }

    onEdit() {
        this.edit.emit(this.entityId);
    }

    onDelete(e: MouseEvent) {
        e.stopPropagation();
        this.delete.emit({id: this.entityId, event: e});
    }
}
