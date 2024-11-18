import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isoDateFormat'
})
export class IsoDateFormatPipe implements PipeTransform {

  transform(value: Date | string): string {
    if(!value){
      return '-';
    }

    let date=new Date();

    if(typeof value === 'string'){
      date = new Date(value);
    } 
    if(value instanceof Date){
      date = value;
    }

    if(isNaN(date.getTime())){
      return '-';
    }
    const monthNames = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    const year = date.getUTCFullYear();
    const month = monthNames[date.getUTCMonth()];
    const day = String(date.getUTCDate()+1).padStart(2,'0');
    return `${day}-${month}-${year}`;
  }

}
