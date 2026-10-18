import { Card } from './card';

export interface Column {
  id: number;
  name: string;
  position: number;
  boardId: number;
}

export interface ColumnDetail {
  id: number;
  name: string;
  position: number;
  cards: Card[];
}
