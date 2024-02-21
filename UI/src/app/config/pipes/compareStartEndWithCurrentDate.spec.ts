import { CompareStartEndWithCurrentDatePipe } from './compareStartEndWithCurrentDate';

describe('CompareStartEndWithCurrentDatePipe', () => {
  let pipe: CompareStartEndWithCurrentDatePipe;

  beforeEach(() => {
    pipe = new CompareStartEndWithCurrentDatePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return true if the current date is between the start and end dates', () => {
    const item = { startDate: '2021-01-01', endDate: '2027-12-31' };
    const result = pipe.transform(item);
    expect(result).toBeTrue();
  });

  it('should return false if the current date is before the start date', () => {
    const item = { startDate: '2022-01-01', endDate: '2022-12-31' };
    const result = pipe.transform(item);
    expect(result).toBeFalse();
  });

  it('should return false if the current date is after the end date', () => {
    const item = { startDate: '2020-01-01', endDate: '2020-12-31' };
    const result = pipe.transform(item);
    expect(result).toBeFalse();
  });

  it('should return false if the item is undefined', () => {
    const item = undefined;
    const result = pipe.transform(item);
    expect(result).toBeFalse();
  });

  it('should return false if the start date is undefined', () => {
    const item = { endDate: '2022-12-31' };
    const result = pipe.transform(item);
    expect(result).toBeFalse();
  });

  it('should return false if the end date is undefined', () => {
    const item = { startDate: '2022-01-01' };
    const result = pipe.transform(item);
    expect(result).toBeFalse();
  });
});
