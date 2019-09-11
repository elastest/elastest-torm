import { ElastestLogAnalyzerComponent } from '../elastest-log-analyzer.component';
import { SearchPatternModel } from './search-pattern/search-pattern-model';
import { Component, Input, OnInit } from '@angular/core';
import { PopupService } from '../../shared/services/popup.service';

@Component({
  selector: 'mark-component',
  templateUrl: './mark.component.html',
  styleUrls: ['./mark.component.scss'],
})
export class MarkComponent implements OnInit {
  @Input()
  public model: ElastestLogAnalyzerComponent;

  patternDefault: SearchPatternModel = new SearchPatternModel();
  patterns: SearchPatternModel[] = [this.patternDefault];
  currentRowSelected: number = -1;
  currentPos: number = -1;

  constructor(private popupService: PopupService) {}

  ngOnInit(): void {}

  addPattern(): void {
    this.patterns.push(new SearchPatternModel());
  }

  removePattern(position: number): void {
    if (position < this.patterns.length - 1) {
      // Not last pattern
      this.patterns.splice(position, 1);
      if (this.patterns.length === 0) {
        this.addPattern();
      } else {
        this.searchByPatterns();
      }
    } else if (
      position === this.patterns.length - 1 &&
      this.patterns[position].searchValue !== '' &&
      this.patterns[position].found < 0
    ) {
      // Last pattern with search message and not searched
      this.patterns.splice(position, 1);
      this.addPattern();
    }
  }

  clearPatterns(): void {
    for (let pattern of this.patterns) {
      pattern.searchValue = '';
      pattern.results = [];
      pattern.found = -1;
      pattern.foundButHidden = false;
    }
    this.currentPos = -1;
    this.currentRowSelected = -1;
    this.cleanRowsColor();
  }

  removeAllPatterns(): void {
    this.patterns = [];
    this.currentPos = -1;
    this.currentRowSelected = -1;
    this.cleanRowsColor();
    this.addPattern();
  }

  markOrClean(index: number): void {
    let pattern: SearchPatternModel = this.patterns[index];
    if (pattern.found < 0 || (pattern.found >= 0 && pattern.foundButHidden)) {
      // If is unmarked, search this pattern to mark
      pattern.foundButHidden = false;
      this.searchByPattern(index);
    } else {
      pattern.foundButHidden = true;
      // pattern.position = -1;
      this.searchByPatterns();
    }
  }

  searchByPatterns(): void {
    this.currentPos = -1;
    this.cleanRowsColor();
    let i: number = 0;
    let rows: any[] = this.model.getRowsData();
    rows.map((row: any) => {
      for (let pattern of this.patterns) {
        if (i === 0) {
          // First iteration of map
          pattern.results = []; // Initialize results to empty
        }
        if (pattern.searchValue !== '' && row.message.toUpperCase().indexOf(pattern.searchValue.toUpperCase()) > -1) {
          if (pattern.results.indexOf(i) === -1) {
            pattern.results.push(i);
          }
        }
      }
      i++;
    });

    let j: number = 0;
    for (let pattern of this.patterns) {
      if (pattern.searchValue !== '') {
        pattern.found = pattern.results.length;
      }
      if (pattern.results.length > 0) {
        this.paintResults(j);
        this.next(j);
      }
      j++;
    }
  }

  searchByPattern(patternId: number): void {
    if (this.patterns[patternId].searchValue !== '') {
      this.searchByPatterns();

      this.clearFocusedRow();
      this.currentPos = -1;

      // Repaint and focus this search
      this.paintResults(patternId);
      this.next(patternId);
      if (patternId === this.patterns.length - 1) {
        this.addPattern();
      }
    } else {
      this.popupService.openSnackBar('Search value can not be empty');
    }
  }

  paintResults(patternId: number): void {
    let rows: any[] = this.model.getRowsData();
    if (rows && rows.length > 0) {
      for (let result of this.patterns[patternId].results) {
        if (rows[result] && !this.patterns[patternId].foundButHidden) {
          rows[result].marked = this.patterns[patternId].color;
        } else {
          rows[result].marked = undefined;
        }
      }
      this.model.redrawRows();
    }
  }

  cleanRowsColor(): void {
    let rows: any[] = this.model.getRowsData();
    for (let row of rows) {
      row.marked = undefined;
      row.focused = false;
    }
    this.model.redrawRows();
  }

  next(patternId: number): void {
    let pattern: SearchPatternModel = this.patterns[patternId];
    if (pattern.results.length > 0) {
      pattern.results.sort(this.sorted);

      if (this.currentPos === -1) {
        pattern.position = 0;
      } else {
        pattern.position = this.getNextPosition(this.currentPos, pattern.results);
        if (pattern.position === -1) {
          pattern.position = 0;
        }
      }
      if (!this.patterns[patternId].foundButHidden) {
        this.focusRow(pattern.results[pattern.position]);
      }
    }
  }

  prev(patternId: number): void {
    let pattern: SearchPatternModel = this.patterns[patternId];
    if (pattern.results.length > 0) {
      pattern.results.sort(this.sorted);

      if (this.currentPos === -1) {
        pattern.position = pattern.results.length - 1;
      } else {
        pattern.position = this.getPrevPosition(this.currentPos, pattern.results);
        if (pattern.position === -1) {
          pattern.position = pattern.results.length - 1;
        }
      }
      if (!this.patterns[patternId].foundButHidden) {
        this.focusRow(pattern.results[pattern.position]);
      }
    }
  }

  focusRow(newPos: number): void {
    this.clearFocusedRow();
    this.currentPos = newPos;
    let rows: any[] = this.model.getRowsData();
    if (rows.length > 0) {
      rows[this.currentPos].focused = true;
      this.model.redrawRows();
      this.model.gridApi.ensureIndexVisible(this.currentPos, 'undefined'); // Make scroll if it's necessary
      this.model.gridApi.setFocusedCell(this.currentPos, 'message'); // It's not necessary with ensureIndexVisible, but highlight message
      this.model.redrawRows();
    }
  }

  clearFocusedRow(): void {
    if (this.currentPos >= 0) {
      let rows: any[] = this.model.getRowsData();
      rows[this.currentPos].focused = false;
      this.model.gridApi.clearFocusedCell();
      this.model.redrawRows();
    }
  }

  getNextPosition(element: number, array: number[]): number {
    let i: number;
    for (i = 0; i < array.length; i++) {
      if (element < array[i]) {
        return i;
      }
    }
    return -1;
  }

  getPrevPosition(element: number, array: number[]): number {
    let i: number;
    for (i = array.length; i >= 0; i--) {
      if (element > array[i]) {
        return i;
      }
    }
    return -1;
  }

  sorted(a: number, b: number): number {
    return a - b;
  }

  openColorPicker(i: number): void {
    document.getElementById('pattern' + i + 'Color').click();
  }
}
