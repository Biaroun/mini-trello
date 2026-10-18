import { Component, computed, ElementRef, afterNextRender, input, OnInit, output, viewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Card, CardPayload, PRIORITIES, PRIORITY_LABELS, Priority } from '../../../core/models/card';
import { todayIso } from '../../../core/utils/date';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-card-dialog',
  styleUrl: './card-dialog.component.css',
  templateUrl: './card-dialog.component.html',
  host: { '(document:keydown.escape)': 'cancelled.emit()' },
})
export class CardDialogComponent implements OnInit {
  // null → création, une carte → modification
  readonly card = input<Card | null>(null);
  readonly errorMessage = input<string | null>(null);

  readonly saved = output<CardPayload>();
  readonly cancelled = output<void>();

  protected readonly isEdit = computed(() => this.card() !== null);
  protected readonly priorities = PRIORITIES;
  protected readonly priorityLabels = PRIORITY_LABELS;
  protected readonly today = todayIso();

  protected readonly form = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(1000)] }),
    priority: new FormControl<Priority>('MEDIUM', { nonNullable: true }),
    dueDate: new FormControl('', { nonNullable: true }),
  });

  private readonly titleInput = viewChild<ElementRef<HTMLInputElement>>('titleInput');

  constructor() {
    afterNextRender(() => this.titleInput()?.nativeElement.focus());
  }

  ngOnInit(): void {
    const card = this.card();
    if (card) {
      this.form.setValue({
        title: card.title,
        description: card.description ?? '',
        priority: card.priority,
        dueDate: card.dueDate ?? '',
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saved.emit({
      title: value.title.trim(),
      description: value.description.trim() || null,
      priority: value.priority,
      dueDate: value.dueDate || null,
    });
  }
}
