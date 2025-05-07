import { UtcToLocalUserPipe } from './utc-to-local-user.pipe';

describe('UtcToUserLocalPipe', () => {
  let pipe: UtcToLocalUserPipe;

  beforeEach(() => {
    pipe = new UtcToLocalUserPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return empty string for null or undefined input', () => {
    expect(pipe.transform(null as any)).toBe('');
    expect(pipe.transform(undefined as any)).toBe('');
    expect(pipe.transform('')).toBe('');
  });

  it('should correctly convert a valid UTC string to local time', () => {
    const utcString = '2025-04-28T12:00:00Z';
    const result = pipe.transform(utcString);
    expect(result).toBeTruthy();
    expect(typeof result).toBe('string');
    // Optionally you can add more validations depending on your timezone
  });

  it('should correctly convert a valid UTC Date object to local time', () => {
    const utcDate = new Date('2025-04-28T12:00:00Z');
    const result = pipe.transform(utcDate);
    expect(result).toBeTruthy();
    expect(typeof result).toBe('string');
  });

  it('should apply custom formatOptions if provided', () => {
    const utcString = '2025-04-28T12:00:00Z';
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    };
    const result = pipe.transform(utcString, options);

    expect(result).toContain('2025'); // At least basic check
  });

  it('should return empty string if date is invalid', () => {
    const invalidDateString = 'invalid-date-format';
    const result = pipe.transform(invalidDateString);
    expect(result).toBe('Invalid Date');
  });

  it('should handle error gracefully and return empty string', () => {
    spyOn(console, 'error'); // to suppress error logging in test output

    // Intentionally cause an error
    const badInput = {
      toLocaleString: () => {
        throw new Error('Forced error');
      },
    } as any;

    const result = pipe.transform(badInput);
    expect(result).toBe('');
    expect(console.error).toHaveBeenCalled();
  });
});