import { Injectable } from '@angular/core';

@Injectable()
export class TableService {
  comparatorClass: string;
  comparedClass: string;
  lastCloseTag: string;
  lastOpenTag: string;
  oldData: string;
  results: any[];

  constructor() {}

  generateTable(diff: string): any[] {
    this.results = [];
    this.comparatorClass = 'normal';
    this.comparedClass = 'normal';
    const lines: string[] = diff.split('<br>');
    // lines.pop();
    let comparedLine: string = '';
    let comparatorLine: string = '';
    let i: number = 1;
    lines.forEach((line: string) => {
      line = this.closeOpenedTags(line.replace('&para;', ''));
      line = this.openClosedTags(line);
      comparatorLine = this.cleanBetweenTags('<ins>', '</ins>', line, 0);
      comparedLine = this.cleanBetweenTags('<del>', '</del>', line, 1);
      this.concatResults(i, comparatorLine, comparedLine);
      i++;
    });
    this.solveUselessDiffs();
    this.solveBasicTableColors();
    // this.solveResultErrors();
    return this.results;
  }

  private cleanBetweenTags(open: string, close: string, line: string, code: number): string {
    let uselessData: string;
    while (line.indexOf(open) !== -1) {
      uselessData = line.substring(line.indexOf(open) + 5, line.indexOf(close));
      line = line.replace(open + uselessData + close, '');
    }
    if (line.length < 2) {
      code === 0 ? (this.comparatorClass = 'added') : (this.comparedClass = 'added');
    }
    return line;
  }

  private closeOpenedTags(line: string): string {
    const targetPos: number = line.lastIndexOf('<');
    const tagStarting: string = line.substring(targetPos, targetPos + 2);
    switch (tagStarting) {
      case '<s':
        line = line + '</span>';
        this.lastCloseTag = '</span>';
        this.lastOpenTag = '<span>';
        break;
      case '<d':
        line = line + '</del>';
        this.lastCloseTag = '</del>';
        this.lastOpenTag = '<del>';
        break;
      case '<i':
        line = line + '</ins>';
        this.lastCloseTag = '</ins>';
        this.lastOpenTag = '<ins>';
        break;
      default:
        line = this.lastOpenTag + line + this.lastCloseTag;
    }
    return line;
  }

  private concatResults(i: number, comparator: string, compared: string): void {
    this.results = this.results.concat({
      index_p: i,
      com_p: {
        content: comparator,
        class: this.comparatorClass,
      },
      indexp: i,
      comp: {
        content: compared,
        class: this.comparedClass,
      },
    });
    this.comparatorClass = 'normal';
    this.comparedClass = 'normal';
  }

  private openClosedTags(line: string): string {
    const targetPos: number = line.indexOf('<');
    const tagStarting: string = line.substring(targetPos, targetPos + 3);
    switch (tagStarting) {
      case '</s':
        line = '<span>' + line;
        break;
      case '</d':
        line = '<del>' + line;
        break;
      case '</i':
        line = '<ins>' + line;
        break;
      default:
        break;
    }
    return line;
  }

  private solveBasicTableColors(): void {
    for (let i: number = 0; i < this.results.length; i++) {
      const result: any = this.results[i];
      if (result.com_p.content.indexOf('<del>') !== -1 && result.com_p !== undefined) {
        result.com_p.class = 'delC';
        result.comp.class = 'added';
      }
      if (result.comp.content.indexOf('<ins>') !== -1 && result.comp !== undefined) {
        result.comp.class = 'insC';
        if (result.com_p.class !== 'delC') {
          result.com_p.class = 'added';
        }
      }
      this.results[i] = result;
    }
  }

  private solveUselessDiffs(): void {
    for (let i: number = 0; i < this.results.length; i++) {
      const result: any = this.results[i];
      let comparator: any = result.com_p.content;
      if (comparator.indexOf('<del>') === comparator.lastIndexOf('<del>') && comparator.indexOf('<del>') === 0) {
        const content: string = comparator.substring(comparator.indexOf('<del>') + 5, comparator.indexOf('</del>'));
        if (/^[^A-Za-z0-9]*$/g.test(content)) {
          comparator = '';
          result.comp.class = 'normal';
        }
      }
      result.com_p.content = comparator;
      let compared: string = result.comp.content;
      if (comparator.indexOf('<ins>') === comparator.lastIndexOf('<ins>') && compared.indexOf('<ins>') === 0) {
        const content: string = compared.substring(compared.indexOf('<ins>') + 5, compared.indexOf('</ins>'));
        if (/^[^A-Za-z0-9]*$/g.test(content)) {
          compared = '';
          result.com_p.class = 'normal';
        }
      }
      result.comp.content = compared;
      this.results[i] = result;
    }
  }
}
