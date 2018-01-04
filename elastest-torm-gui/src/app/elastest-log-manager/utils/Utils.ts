/*
 * (C) Copyright 2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

export function dateToInputLiteral(date: Date): string {
  return date.getUTCFullYear() + '-' +
    pad(date.getUTCMonth() + 1, 2) + '-' +
    pad(date.getUTCDate(), 2) + 'T' +
    pad(date.getUTCHours(), 2) + ':' +
    pad(date.getMinutes(), 2) + ':' +
    pad(date.getSeconds(), 2);
}

function pad(n: any, width: number, z?: string) {
  z = z || '0';
  n = n + '';
  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

export function minDate(date1: Date, date2: Date): Date {
  if (date1 === undefined) {
    return date2;
  }

  if (date2 === undefined) {
    return date1;
  }

  let min: Date;
  date1 > date2 ? min = date2 : min = date1;
  return min;
}

export function maxDate(date1: Date, date2: Date): Date {
  if (date1 === undefined) {
    return date2;
  }

  if (date2 === undefined) {
    return date1;
  }

  let max: Date;
  date1 < date2 ? max = date2 : max = date1;
  return max;
}

export function invertColor(hex: string, bw: boolean): string {
  if (hex.indexOf('#') === 0) {
    hex = hex.slice(1);
  }
  // convert 3-digit hex to 6-digits.
  if (hex.length === 3) {
    hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
  }
  if (hex.length !== 6) {
    throw new Error('Invalid HEX color.');
  }
  let r: any = parseInt(hex.slice(0, 2), 16);
  let g: any = parseInt(hex.slice(2, 4), 16);
  let b: any = parseInt(hex.slice(4, 6), 16);
  if (bw) {
    return (r * 0.299 + g * 0.587 + b * 0.114) > 186
      ? '#000000'
      : '#FFFFFF';
  }
  // invert color components
  r = (255 - r).toString(16);
  g = (255 - g).toString(16);
  b = (255 - b).toString(16);
  // pad each with zeros and return
  return '#' + padZero(r) + padZero(g) + padZero(b);
}

function padZero(str: string, len?: number): string {
  len = len || 2;
  let zeros: string = new Array(len).join('0');
  return (zeros + str).slice(-len);
}