import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'comparedates'
})
export class CompareStartEndWithCurrentDatePipe implements PipeTransform {

  transform(item): any {
    const currentDate = new Date();
    const start = new Date(item?.startDate);
    const end = new Date(item?.endDate);
    if (currentDate >= start && currentDate <= end) {
      return true;
    }
    return false;
  }

}
