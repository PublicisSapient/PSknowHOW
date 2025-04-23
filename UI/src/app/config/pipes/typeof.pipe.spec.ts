import { TypeofPipe } from './type-of.pipe';

describe('TypeofPipe', () => {
  let pipe: TypeofPipe;

  beforeEach(() => {
    pipe = new TypeofPipe();
  });

  it('should return "string" for a string input', () => {
    expect(pipe.transform('test')).toBe('string');
  });

  it('should return "number" for a number input', () => {
    expect(pipe.transform(123)).toBe('number');
  });

  it('should return "boolean" for a boolean input', () => {
    expect(pipe.transform(true)).toBe('boolean');
  });

  it('should return "object" for an object input', () => {
    expect(pipe.transform({})).toBe('object');
  });

  it('should return "undefined" for an undefined input', () => {
    expect(pipe.transform(undefined)).toBe('undefined');
  });

  it('should return "function" for a function input', () => {
    expect(pipe.transform(() => {})).toBe('function');
  });

  it('should return "symbol" for a symbol input', () => {
    expect(pipe.transform(Symbol())).toBe('symbol');
  });

  it('should return "object" for a null input', () => {
    expect(pipe.transform(null)).toBe('object');
  });
});
