import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Board } from '../../core/models/board';
import { BoardService } from '../../core/services/board.service';

// Un nom est valide s'il contient au moins un caractère visible (max 100, comme côté API)
const nameValidators = [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)];

@Component({
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  selector: 'app-board-list',
  styleUrl: './board-list.component.css',
  templateUrl: './board-list.component.html',
})
export class BoardListComponent implements OnInit {
  private readonly boardService = inject(BoardService);

  protected readonly boards = signal<Board[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly editingId = signal<number | null>(null);

  protected readonly createForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: nameValidators }),
  });
  protected readonly editControl = new FormControl('', { nonNullable: true, validators: nameValidators });

  ngOnInit(): void {
    this.boardService.getAll().subscribe({
      next: (boards) => this.boards.set(boards),
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected create(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.boardService.create(this.createForm.controls.name.value.trim()).subscribe({
      next: (board) => {
        this.boards.update((boards) => [...boards, board]);
        this.createForm.reset();
        this.error.set(null);
      },
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected startEdit(board: Board): void {
    this.editControl.setValue(board.name);
    this.editingId.set(board.id);
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
  }

  protected saveEdit(board: Board): void {
    if (this.editControl.invalid) {
      return;
    }

    this.boardService.rename(board.id, this.editControl.value.trim()).subscribe({
      next: (updated) => {
        this.boards.update((boards) => boards.map((b) => (b.id === updated.id ? updated : b)));
        this.editingId.set(null);
        this.error.set(null);
      },
      error: (err: Error) => this.error.set(err.message),
    });
  }

  protected remove(board: Board): void {
    if (!confirm(`Supprimer le tableau « ${board.name} » avec toutes ses colonnes et ses cartes ?`)) {
      return;
    }

    this.boardService.delete(board.id).subscribe({
      next: () => {
        this.boards.update((boards) => boards.filter((b) => b.id !== board.id));
        this.error.set(null);
      },
      error: (err: Error) => this.error.set(err.message),
    });
  }
}
