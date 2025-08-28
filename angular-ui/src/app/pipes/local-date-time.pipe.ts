import {Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';

@Pipe({name: 'localDateTime'})
export class LocalDateTimePipe implements PipeTransform {
    constructor(private datePipe: DatePipe) {
    }

    transform(value: string | Date | null | undefined, format: string = 'medium'): string | null {
        if (!value) return null;
        let date: Date;
        if (typeof value === 'string') {
            // 1) handle comma-separated fields: "YYYY,MM,DD,hh,mm,ss,nanosecond"
            if (/^\d+(,\d+)+$/.test(value)) {
                const parts = value.split(',').map(n => parseInt(n, 10));
                const [year, month, day, hour, minute, second, nano = 0] = parts;
                const ms = Math.floor(nano / 1e6);
                date = new Date(year, month - 1, day, hour, minute, second, ms);

                // 2) try ISO/T fallback
            } else {
                date = new Date(value);
                if (isNaN(date.getTime())) {
                    const iso = value.replace(' ', 'T');
                    date = new Date(iso);
                }
            }

            if (isNaN(date.getTime())) {
                // give up and return the raw string
                return value;
            }

        } else if (typeof value === 'object') {
            const objectToString = value.toString();
            const parts = objectToString.split(',').map(n => parseInt(n));
            const [year, month, day, hour, minute, second, nano = 0] = parts.length === 7 ? parts : parts.concat(0);
            const ms = Math.floor(nano / 1e6);

            date = new Date(year, month - 1, day, hour, minute, second, ms);
        } else {
            date = value;
        }
        return this.datePipe.transform(date, format);
    }
}
