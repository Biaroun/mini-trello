import { ColumnDetail } from './column';

export interface Board {
  id: number;
  name: string;
  createdAt: string;
}

export interface BoardDetail extends Board {
  columns: ColumnDetail[];
}
