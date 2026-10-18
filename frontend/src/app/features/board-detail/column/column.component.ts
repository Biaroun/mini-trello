import { CdkDrag, CdkDragDrop, CdkDropList } from '@angular/cdk/drag-drop';
import { Component, input, output, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Card } from '../../../core/models/card';
import { ColumnDetail } from '../../../core/models/column';
import { CardComponent } from '../card/card.component';

@Component({
  imports: [CdkDropList, CdkDrag, ReactiveFormsModule, CardComponent],
  selector: 'app-column',
  styleUrl: './column.component.css',
  templateUrl: './column.component.html',
})
export class ColumnComponent {
  readonly column = input.required<ColumnDetail>();

  readonly renamed = output<string>();
  readonly removed = output<void>();
  readonly addCard = output<void>();
  readonly editCard = output<Card>();
  readonly removeCard = output<Card>();
  readonly cardDropped = output<CdkDragDrop<number, number, Card>>();

  protected readonly editing = signal(false);
  protected readonly nameControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(50)],
  });

  protected startEdit(): void {
    this.nameControl.setValue(this.column().name);
    this.editing.set(true);
  }

  protected cancelEdit(): void {
    this.editing.set(false);
  }

  protected saveEdit(): void {
    if (this.nameControl.invalid) {
      return;
    }

    const name = this.nameControl.value.trim();
    this.editing.set(false);
    if (name !== this.column().name) {
      this.renamed.emit(name);
    }
  }
}
