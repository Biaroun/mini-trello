import { CdkDragDrop, CdkDropListGroup } from '@angular/cdk/drag-drop';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BoardDetail } from '../../core/models/board';
import { Card, CardPayload } from '../../core/models/card';
import { BoardService } from '../../core/services/board.service';
import { CardService } from '../../core/services/card.service';
import { ColumnService } from '../../core/services/column.service';
import { CardDialogComponent } from './card-dialog/card-dialog.component';
import { ColumnComponent } from './column/column.component';

// Carte en cours de création (card = null) ou de modification, dans la colonne columnId
interface DialogState {
  columnId: number;
  card: Card | null;
}

@Component({
  imports: [RouterLink, ReactiveFormsModule, CdkDropListGroup, ColumnComponent, CardDialogComponent],
  selector: 'app-board-detail',
  styleUrl: './board-detail.component.css',
  templateUrl: './board-detail.component.html',
})
export class BoardDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly boardService = inject(BoardService);
  private readonly columnService = inject(ColumnService);
  private readonly cardService = inject(CardService);

  private readonly boardId = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly board = signal<BoardDetail | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly editingTitle = signal(false);
  protected readonly dialog = signal<DialogState | null>(null);
  protected readonly dialogError = signal<string | null>(null);

  protected readonly titleControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
  });
  protected readonly columnForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(50)],
    }),
  });

  ngOnInit(): void {
    this.load();
  }

  // ---------- Tableau ----------

  protected startEditTitle(board: BoardDetail): void {
    this.titleControl.setValue(board.name);
    this.editingTitle.set(true);
  }

  protected saveTitle(board: BoardDetail): void {
    if (this.titleControl.invalid) {
      return;
    }

    const name = this.titleControl.value.trim();
    this.editingTitle.set(false);
    if (name === board.name) {
      return;
    }
    this.boardService.rename(board.id, name).subscribe({
      next: () => this.load(),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected removeBoard(board: BoardDetail): void {
    if (!confirm(`Supprimer le tableau « ${board.name} » avec toutes ses colonnes et ses cartes ?`)) {
      return;
    }
    this.boardService.delete(board.id).subscribe({
      next: () => this.router.navigate(['/boards']),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  // ---------- Colonnes ----------

  protected addColumn(): void {
    if (this.columnForm.invalid) {
      this.columnForm.markAllAsTouched();
      return;
    }

    this.columnService.create(this.boardId, this.columnForm.controls.name.value.trim()).subscribe({
      next: () => {
        this.columnForm.reset();
        this.load();
      },
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected renameColumn(columnId: number, name: string): void {
    this.columnService.rename(columnId, name).subscribe({
      next: () => this.load(),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected removeColumn(columnId: number, name: string): void {
    if (!confirm(`Supprimer la colonne « ${name} » et toutes ses cartes ?`)) {
      return;
    }
    this.columnService.delete(columnId).subscribe({
      next: () => this.load(),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  // ---------- Cartes ----------

  protected openCreateDialog(columnId: number): void {
    this.dialogError.set(null);
    this.dialog.set({ columnId, card: null });
  }

  protected openEditDialog(card: Card): void {
    this.dialogError.set(null);
    this.dialog.set({ columnId: card.columnId, card });
  }

  protected closeDialog(): void {
    this.dialog.set(null);
  }

  protected saveCard(state: DialogState, payload: CardPayload): void {
    const request = state.card
      ? this.cardService.update(state.card.id, payload)
      : this.cardService.create(state.columnId, payload);

    request.subscribe({
      next: () => {
        this.dialog.set(null);
        this.load();
      },
      error: (err: Error) => this.dialogError.set(err.message),
    });
  }

  protected removeCard(card: Card): void {
    if (!confirm(`Supprimer la carte « ${card.title} » ?`)) {
      return;
    }
    this.cardService.delete(card.id).subscribe({
      next: () => this.load(),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  // Glisser-déposer : on met l'écran à jour tout de suite, puis on prévient l'API.
  // Si elle refuse, on recharge l'état réel du serveur.
  protected moveCard(event: CdkDragDrop<number, number, Card>): void {
    const card = event.item.data;
    const sourceColumnId = event.previousContainer.data;
    const targetColumnId = event.container.data;
    const position = event.currentIndex;

    if (sourceColumnId === targetColumnId && event.previousIndex === position) {
      return;
    }

    this.board.update((board) => board && withCardMoved(board, card.id, targetColumnId, position));
    this.cardService.move(card.id, targetColumnId, position).subscribe({
      error: (err: Error) => {
        this.error.set(err.message);
        this.load();
      },
    });
  }

  // ---------- Chargement ----------

  private load(): void {
    this.boardService.getById(this.boardId).subscribe({
      next: (board) => {
        this.board.set(board);
        this.error.set(null);
      },
      error: (err: Error) => this.error.set(err.message),
    });
  }
}

// Même logique que le backend : on retire la carte, on l'insère à la position demandée,
// puis on renumérote (0, 1, 2…) pour que les positions restent continues.
function withCardMoved(board: BoardDetail, cardId: number, targetColumnId: number, position: number): BoardDetail {
  const card = board.columns.flatMap((column) => column.cards).find((c) => c.id === cardId);
  if (!card) {
    return board;
  }

  const columns = board.columns.map((column) => {
    let cards = column.cards.filter((c) => c.id !== cardId);
    if (column.id === targetColumnId) {
      cards = [...cards.slice(0, position), { ...card, columnId: targetColumnId }, ...cards.slice(position)];
    }
    return { ...column, cards: cards.map((c, index) => ({ ...c, position: index })) };
  });
  return { ...board, columns };
}
