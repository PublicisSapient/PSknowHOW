import { NamePipePipe } from './name-pipe.pipe';

describe('Pipe: Default', () => {
  let pipe: NamePipePipe;

  beforeEach(() => {
    pipe = new NamePipePipe();
  });

  it('should transform value correctly when input is not "project" and hierarchyData is available', () => {
    const hierarchyData = [
      {
        level: 1,
        hierarchyLevelId: 'hierarchyLevelOne',
        hierarchyLevelIdName: 'Level One',
        suggestions: [
          { name: 'Sample One', code: 'Sample One' },
          { name: 'Sample Two', code: 'Sample Two' },
          { name: 'T1', code: 'T1' },
        ],
        value: '',
        required: true,
      },
      {
        level: 2,
        hierarchyLevelId: 'hierarchyLevelTwo',
        hierarchyLevelIdName: 'Level Two',
        suggestions: [
          { name: 'Sample Four', code: 'Sample Four' },
          { name: 'Sample Three', code: 'Sample Three' },
          { name: 'T2', code: 'T2' },
        ],
        value: '',
        required: true,
      },
      {
        level: 3,
        hierarchyLevelId: 'hierarchyLevelThree',
        hierarchyLevelIdName: 'Level Three',
        suggestions: [
          { name: 'Sample Five', code: 'Sample Five' },
          { name: 'Sample Six', code: 'Sample Six' },
          { name: 'T3', code: 'T3' },
        ],
        value: '',
        required: true,
      },
    ];
    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
    expect(pipe.transform('')).toBe('');
    expect(pipe.transform('Project')).toBe('Project');
    expect(pipe.transform('hierarchyLevelTwo')).toBe('Level Two');
  });

  it('should transform value correctly when input is not "project" and hierarchyData is not available', () => {
    localStorage.removeItem('hierarchyData');
    const transformedValue = pipe.transform('hierarchyLevelThree');
    expect(transformedValue).toEqual('hierarchyLevelThree');
  });

  it('should transform value to "Project" when input is "project"', () => {
    const transformedValue = pipe.transform('project');
    expect(transformedValue).toEqual('Project');
  });
});
