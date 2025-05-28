import { DatePipe } from '@angular/common';
import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'UtcToLocalUserTimeZone',
})
@Injectable({ providedIn: 'root' })
export class UtcToLocalUserPipe implements PipeTransform {
  transform(
    utcDate: string,
    formatOptions?: string,
  ): string {
    if (!utcDate) {
      return '';
    }

    if (utcDate === '-') {
      return '-';
    }

    const regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?::\d{2,6})?Z$/;
    if(!regex.test(utcDate)){
       return utcDate;
    }

    try {
      return new DatePipe('en-US').transform(
        utcDate,
        formatOptions || 'dd-MMM-yyyy',
      );
    } catch (error) {
      console.error('Error in utcToLocal pipe:', error);
      return '';
    }
  }
}