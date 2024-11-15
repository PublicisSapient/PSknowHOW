import { TestBed } from '@angular/core/testing';
import { IsoDateFormatPipe } from './iso-date-format.pipe';

describe('IsoDateFormatPipe', () => {
  let pipe: IsoDateFormatPipe;

  beforeEach(()=>{
    TestBed.configureTestingModule({
			providers: [IsoDateFormatPipe]
		});
		pipe = TestBed.inject(IsoDateFormatPipe);
  });
  it('create an instance', () => {
    const pipe = new IsoDateFormatPipe();
    expect(pipe).toBeTruthy();
  });

  it('should transform a valid Date object to the correct ISO date format', () => {
		const date = new Date('2023-10-01T00:00:00Z');
		const result = pipe.transform(date);
		expect(result).toBe('02-OCT-2023');
	});

  it('should transform a valid date string to ISO format', () => {
		const inputDate = '2023-10-01';
		const expectedOutput = '02-OCT-2023';
		const result = pipe.transform(inputDate);
		expect(result).toBe(expectedOutput);
	});


	it('should return empty string for an empty string', () => {
		const result = pipe.transform('');
		expect(result).toBe('-');
	});

	it('should return Invalid Date for an invalid date string', () => {
		const result = pipe.transform('invalid-date-string');
		expect(result).toBe('-');
	});

	it('should return empty string for null value', () => {
		const result = pipe.transform(null);
		expect(result).toBe('-');
	});

	
});
