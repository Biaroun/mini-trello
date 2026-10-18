import { DatePipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { Card, PRIORITY_LABELS } from '../../../core/models/card';
import { todayIso } from '../../../core/utils/date';

@Component({
  imports: [DatePipe],
  selector: 'app-card',
  styleUrl: './card.component.css',
  templateUrl: './card.component.html',
})
export class CardComponent {
  readonly card = input.required<Card>();

  readonly edit = output<void>();
  readonly remove = output<void>();

  protected readonly priorityLabel = computed(() => PRIORITY_LABELS[this.card().priority]);

  // Les dates ISO (yyyy-MM-dd) se comparent correctement comme du texte
  protected readonly overdue = computed(() => {
    const dueDate = this.card().dueDate;
    return dueDate !== null && dueDate < todayIso();
  });
}
