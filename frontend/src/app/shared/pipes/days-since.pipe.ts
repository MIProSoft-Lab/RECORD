import { Pipe, PipeTransform } from '@angular/core';

const MS_PER_DAY = 86_400_000;

/**
 * Devuelve el número de días completos transcurridos entre [value] y ahora (o entre
 * [value] y [until] si se indica). Se calcula en el frontend a partir de la fecha de
 * cambio de estado, sin necesidad de que el backend exponga el cómputo. Valores futuros
 * devuelven 0.
 */
@Pipe({ name: 'daysSince' })
export class DaysSincePipe implements PipeTransform {
  transform(value: string | Date | null | undefined, until?: string | Date): number {
    if (!value) return 0;
    const from = new Date(value).getTime();
    if (Number.isNaN(from)) return 0;
    const to = until ? new Date(until).getTime() : Date.now();
    return Math.max(0, Math.floor((to - from) / MS_PER_DAY));
  }
}
