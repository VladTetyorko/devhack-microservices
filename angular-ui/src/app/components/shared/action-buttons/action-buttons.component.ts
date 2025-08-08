import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
    selector: 'app-action-buttons',
    templateUrl: './action-buttons.component.html',
    styleUrls: ['./action-buttons.component.scss']
})
export class ActionButtonsComponent {
    @Input() entityId!: string | number;
    @Input() showView: boolean = true;
    @Input() showEdit: boolean = true;
    @Input() showDelete: boolean = true;

    @Output() view = new EventEmitter<string | number>();
    @Output() edit = new EventEmitter<string | number>();
    @Output() delete = new EventEmitter<{ id: string | number, event: MouseEvent }>();

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
