import { ScrumKanbanPipe } from './scrumKanbanPipe';

describe('ScrumKanbanPipe', () => {
  let pipe: ScrumKanbanPipe;

  beforeEach(() => {
    pipe = new ScrumKanbanPipe();
  });

  it('should return Kanban when value passed as true', () => {
    const returnValue = pipe.transform(true);
    expect(returnValue).toBe('Kanban');
  });

  it('should return Scrum when value passed as false', () => {
    const returnValue = pipe.transform(false);
    expect(returnValue).toBe('Scrum');
  });
});
