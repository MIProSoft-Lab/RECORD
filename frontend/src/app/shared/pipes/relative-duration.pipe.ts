import { Pipe, PipeTransform, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

const MS_PER_MINUTE = 60_000;
const MINUTES_PER_HOUR = 60;
const HOURS_PER_DAY = 24;

/**
 * Formatea la duración entre [from] y [to] (o ahora) con granularidad adaptativa:
 * menos de una hora → minutos; menos de un día → horas y minutos; a partir de un día →
 * días y horas. Se localiza vía COMMON.DURATION.*. Es impure para reaccionar al cambio
 * de idioma y al paso del tiempo, igual que el pipe `translate`.
 */
@Pipe({ name: 'relativeDuration', pure: false })
export class RelativeDurationPipe implements PipeTransform {
  private readonly translate = inject(TranslateService);

  transform(from: string | Date | null | undefined, to?: string | Date): string {
    if (!from) return '';
    const fromMs = new Date(from).getTime();
    if (Number.isNaN(fromMs)) return '';
    const toMs = to ? new Date(to).getTime() : Date.now();
    const totalMinutes = Math.max(0, Math.floor((toMs - fromMs) / MS_PER_MINUTE));

    if (totalMinutes < MINUTES_PER_HOUR) {
      return this.part(totalMinutes, 'MINUTE');
    }

    const totalHours = Math.floor(totalMinutes / MINUTES_PER_HOUR);
    if (totalHours < HOURS_PER_DAY) {
      const minutes = totalMinutes % MINUTES_PER_HOUR;
      return this.join(this.part(totalHours, 'HOUR'), minutes ? this.part(minutes, 'MINUTE') : null);
    }

    const days = Math.floor(totalHours / HOURS_PER_DAY);
    const hours = totalHours % HOURS_PER_DAY;
    return this.join(this.part(days, 'DAY'), hours ? this.part(hours, 'HOUR') : null);
  }

  private part(count: number, unit: 'MINUTE' | 'HOUR' | 'DAY'): string {
    const key = `COMMON.DURATION.${unit}${count === 1 ? '' : 'S'}`;
    return this.translate.instant(key, { count });
  }

  private join(first: string, second: string | null): string {
    return second ? `${first} ${second}` : first;
  }
}
