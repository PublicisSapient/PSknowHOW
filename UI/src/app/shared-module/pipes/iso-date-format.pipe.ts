import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isoDateFormat',
})
export class IsoDateFormatPipe implements PipeTransform {
  transform(value: Date | string): string {
    let matches = false;
    if (!value) {
      return '';
    }

    let date: any;
    let time = '';

    if (typeof value === 'string') {
      date = new Date(value);
      const regex = /^(\d{1,2}-(\d{2}|[a-zA-Z]{3})-\d{4}|\d{4}-\d{2}-\d{2})$/i;
      matches = regex.test(value.trim());
    }
    if (value instanceof Date) {
      date = value;
    }

    if (isNaN(date.getTime())) {
      return '';
    } else {
      time =
        date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
    }
    const monthNames = [
      'JAN',
      'FEB',
      'MAR',
      'APR',
      'MAY',
      'JUN',
      'JUL',
      'AUG',
      'SEP',
      'OCT',
      'NOV',
      'DEC',
    ];
    const year = date.getFullYear();
    const month = monthNames[date.getMonth()];
    const day = String(date.getDate()).padStart(2, '0');

    return `${day}-${month}-${year} ${matches ? '' : time}`;
  }
}
