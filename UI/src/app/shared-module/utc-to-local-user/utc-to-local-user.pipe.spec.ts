import { UtcToLocalUserPipe } from './utc-to-local-user.pipe';

describe('UtcToUserLocalPipe', () => {
  let pipe: UtcToLocalUserPipe;

  beforeEach(() => {
    pipe = new UtcToLocalUserPipe();
  });

  it('should create the pipe instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return formatted date (default format)', () => {
    const result = pipe.transform('2025-05-12T22:00:00Z');
    // Output depends on your system timezone. We check against format
    expect(result).toMatch(/\d{2}-[A-Za-z]{3}-\d{4}/);
  });

  it('should return formatted date with custom format', () => {
    const result = pipe.transform('2025-05-12T22:00:00Z', 'yyyy/MM/dd');
    expect(result).toMatch(/\d{4}\/\d{2}\/\d{2}/);
  });

  it('should return empty string for null input', () => {
    expect(pipe.transform(null)).toBe('');
  });

  it('should return empty string for invalid date input', () => {
    expect(pipe.transform('invalid-date')).toBe('');
  });
});