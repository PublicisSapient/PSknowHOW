import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isoDateFormat'
})
export class IsoDateFormatPipe implements PipeTransform {

  transform(value: Date | string, format: string = 'yyyy-MM-dd'): string {
    const date = new Date(value);
    if(isNaN(date.getTime())){
      return 'Invalid Date';
    }
    return date.toISOString().split('T')[0];
  }

}
