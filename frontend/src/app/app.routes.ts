import { Routes } from '@angular/router';
import { BoardDetailComponent } from './features/board-detail/board-detail.component';
import { BoardListComponent } from './features/board-list/board-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'boards', pathMatch: 'full' },
  { path: 'boards', component: BoardListComponent },
  { path: 'boards/:id', component: BoardDetailComponent },
];
