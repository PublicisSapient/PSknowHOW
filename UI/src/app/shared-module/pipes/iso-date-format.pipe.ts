import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isoDateFormat'
})
export class IsoDateFormatPipe implements PipeTransform {

  transform(value: Date | string): string {
    if(!value){
      return 'Invalid Date input';
    }

    let date=new Date();

    if(typeof value === 'string'){
      date = new Date(value);
    } 
    if(value instanceof Date){
      date = value;
    }

    if(isNaN(date.getTime())){
      return 'Invalid Date';
    }
    const year = date.getUTCFullYear()
    const month = String(date.getUTCMonth()+1).padStart(2,'0');
    const day = String(date.getUTCDate()).padStart(2,'0');
    return `${year}-${month}-${day}`;
  }

}
