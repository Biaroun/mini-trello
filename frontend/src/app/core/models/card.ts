export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH'];

export const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: 'Basse',
  MEDIUM: 'Moyenne',
  HIGH: 'Haute',
};

export interface Card {
  id: number;
  title: string;
  description: string | null;
  priority: Priority;
  dueDate: string | null;
  position: number;
  columnId: number;
  createdAt: string;
}

// Corps envoyé à l'API pour créer ou modifier une carte
export interface CardPayload {
  title: string;
  description?: string | null;
  priority?: Priority;
  dueDate?: string | null;
}
