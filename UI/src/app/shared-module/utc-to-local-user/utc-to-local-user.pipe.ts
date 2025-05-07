import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'UtcToLocalUserTimeZone',
})
export class UtcToLocalUserPipe implements PipeTransform {
  transform(
    utcDate: string | Date,
    formatOptions?: Intl.DateTimeFormatOptions,
  ): string {
    if (!utcDate) {
      return '';
    }

    try {
      // Step 1: Get user's browser timezone and locale
      const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const userLocale = navigator.language || 'en-US';

      // Step 2: Convert UTC date into user's local timezone and locale
      const dateObj = typeof utcDate === 'string' ? new Date(utcDate) : utcDate;

      return dateObj.toLocaleString(userLocale, {
        timeZone: userTimeZone,
        ...(formatOptions || {}), // If user passes custom format options
      });
    } catch (error) {
      console.error('Error in utcToLocal pipe:', error);
      return '';
    }
  }
}