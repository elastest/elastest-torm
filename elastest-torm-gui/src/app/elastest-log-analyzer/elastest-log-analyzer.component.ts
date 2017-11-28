import { SearchPatternModel } from './search-pattern/search-pattern-model';
import { TreeCheckElementModel } from '../shared/ag-tree-model';
import { LogAnalyzerModel } from './log-analyzer-model';
import { GetIndexModalComponent } from '../elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestESService } from '../shared/services/elastest-es.service';
import { ESQueryModel, ESSearchModel, ESTermModel } from '../shared/elasticsearch-model';
import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { dateToInputLiteral } from './utils/Utils';
import { MdDialog, MdDialogRef } from '@angular/material';
import { ColumnApi, GridApi, GridOptions } from 'ag-grid/main';
import { ITreeOptions, IActionMapping } from 'angular-tree-component';
import { TreeComponent } from 'angular-tree-component';


@Component({
  selector: 'elastest-log-analyzer',
  templateUrl: './elastest-log-analyzer.component.html',
  styleUrls: ['./elastest-log-analyzer.component.scss']
})
export class ElastestLogAnalyzerComponent implements OnInit, AfterViewInit {
  private gridApi: GridApi;
  private gridColumnApi: ColumnApi;

  public esSearchModel: ESSearchModel;
  public logAnalyzer: LogAnalyzerModel;
  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();

  public logRows: any[] = [];
  public logColumns: any[] = [];
  public gridOptions: GridOptions = {
    rowHeight: 22,
    headerHeight: 42,
    suppressRowClickSelection: true,
  };

  @ViewChild('fromDate') fromDate: ElementRef;
  @ViewChild('toDate') toDate: ElementRef;

  // Buttons
  public showLoadMore: boolean = false;
  public showPauseTail: boolean = false;
  public showClearData: boolean = false;

  // Filters
  @ViewChild('componentsTree') componentsTree: TreeComponent;
  @ViewChild('levelsTree') levelsTree: TreeComponent;

  // Mark
  patternDefault: SearchPatternModel = new SearchPatternModel();
  patterns: SearchPatternModel[] = [this.patternDefault];
  currentRowSelected: number = -1;
  currentPos: number = -1;

  constructor(
    public dialog: MdDialog, private elastestESService: ElastestESService,
  ) {
    this.openSelectExecutions();
  }

  ngOnInit() {
    this.logAnalyzer = new LogAnalyzerModel();
    this.initStreamTypeTerm();
    this.initESModel();
  }

  ngAfterViewInit() {
    this.fromDate.nativeElement.value = this.getDefaultFromValue();
    this.toDate.nativeElement.value = this.getDefaultToValue();
  }

  /***** INIT *****/

  // Function to init some parameters of ag-grid
  onGridReady(params): void {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;

    this.gridApi.sizeColumnsToFit();
  }

  initStreamTypeTerm(): void {
    this.streamTypeTerm.name = 'stream_type';
    this.streamTypeTerm.value = this.streamType;
  }

  initESModel(): void {
    this.esSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    this.esSearchModel.body.query.bool.must.termList.push(this.streamTypeTerm);
    this.esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
  }


  /***** Load Log *****/

  prepareLoadLog(): void {
    this.initESModel();

    this.logAnalyzer.fromDate = this.getFromDate();
    this.logAnalyzer.toDate = this.getToDate();

    this.esSearchModel.indices = this.logAnalyzer.selectedIndices;
    this.esSearchModel.filterPathList = this.elastestESService.getBasicFilterFields(this.streamType);
    this.esSearchModel.body.size = this.logAnalyzer.maxResults;
    this.setRange();

    this.setTerms();
  }

  loadLog(): void {
    this.prepareLoadLog();
    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: string = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody)
      .subscribe(
      (data: any) => {
        this.logRows = this.esSearchModel.getDataListFromRaw(data);
        // let rows: any[] = this.esSearchModel.getDataListFromRaw(data);
        // if (this.logRows.length === 0) {
        //   this.logRows = rows;
        // } else if (this.gridApi !== undefined) { // TODO Make working...
        //   this.gridApi.redrawRows({ rowNodes: rows });
        //   this.gridApi.sizeColumnsToFit();
        // }

        let logsLoaded: boolean = this.logRows.length > 0;
        if (logsLoaded) {
          this.elastestESService.popupService.openSnackBar('Logs has been loaded');
          this.setTableHeader();
        } else {
          this.elastestESService.popupService.openSnackBar('There aren\'t logs to load', 'OK');
        }
        this.updateButtons(logsLoaded);
      }
      );
  }

  setRange(): void {
    this.esSearchModel.body.query.bool.must.range.field = '@timestamp';
    this.esSearchModel.body.query.bool.must.range.gte = this.getFromDate();
    this.esSearchModel.body.query.bool.must.range.lte = this.getToDate();
  }

  setTerms(): void {
    if (!this.logAnalyzer.componentsStreams.empty()) {

    }

    if (!this.logAnalyzer.levels.empty()) {
      this.esSearchModel.body.query.bool.must.addTermListToTermList(this.logAnalyzer.getLevelsTermList());

    }
  }

  setTableHeader(): void {
    this.logColumns = [];

    for (let field of this.esSearchModel.filterPathList) {
      if (field !== 'stream_type' && field !== 'type') { // stream_type is always log
        this.logColumns.push(
          { headerName: field, field: field, width: 260, suppressSizeToFit: false, },
        );
      }
    }
  }

  updateButtons(show: boolean): void {
    this.showLoadMore = show;
    this.showClearData = show;
  }

  /***** Dates *****/

  public getDefaultFromValue(): string {
    return dateToInputLiteral(this.logAnalyzer.getDefaultFromDate());
  }

  public getDefaultToValue(): string {
    return dateToInputLiteral(this.logAnalyzer.getDefaultToDate());
  }

  public getFromDate(): any {
    return this.fromDate.nativeElement.value;
  }

  public getToDate(): any {
    return this.toDate.nativeElement.value;
  }

  public setFromDate(date: Date): void {
    this.fromDate.nativeElement.value = dateToInputLiteral(date);
  }

  public setToDate(date: Date): void {
    this.toDate.nativeElement.value = dateToInputLiteral(date);
  }

  public setUseTail(tail: boolean): void {
    this.logAnalyzer.tail = tail;
  }

  public clearData(): void {
    this.logRows = [];
    this.logColumns = [];
    // clearInterval(this.tailInterval);
    // this.tailInterval = undefined;
    this.showLoadMore = false;
    this.showPauseTail = false;
    this.showClearData = false;
    // this.removeAllPatterns();
    // this.dataForAdding = undefined;
  }


  /**** Modal ****/
  public openSelectExecutions(): void {
    let dialogRef: MdDialogRef<GetIndexModalComponent> = this.dialog.open(GetIndexModalComponent, {
      height: '80%',
      width: '90%',
    });
    dialogRef.afterClosed()
      .subscribe(
      (data: any) => {
        if (data) { // Ok Pressed
          if (data.selectedIndices.length > 0 && data.selectedIndices !== '') {
            this.logAnalyzer.selectedIndices = data.selectedIndices;
            if (data.fromDate) {
              this.setFromDate(data.fromDate);
            }
            this.loadComponentStreams();
            this.loadLevels();
          } else {
            this.elastestESService.popupService.openSnackBar('No execution was selected. Selected all by default');
          }
          this.loadLog();
        } else { }
      },
    );
  }

  loadComponentStreams(): void {
    let componentStreamQuery: ESQueryModel = new ESQueryModel();
    componentStreamQuery.bool.must.termList.push(this.streamTypeTerm);

    this.elastestESService.getIndexComponentStreamList(
      this.logAnalyzer.selectedIndicesToString(), componentStreamQuery.convertToESFormat()
    ).subscribe(
      (componentsStreams: any[]) => {
        this.logAnalyzer.setComponentsStreams(componentsStreams);
        this.componentsTree.treeModel.update();
      }
      );
  }

  loadLevels(): void {
    let levelsQuery: ESQueryModel = new ESQueryModel();
    levelsQuery.bool.must.termList.push(this.streamTypeTerm);

    this.elastestESService.getIndexLevel(
      this.logAnalyzer.selectedIndicesToString(), levelsQuery.convertToESFormat()
    ).subscribe(
      (levels: any[]) => {
        this.logAnalyzer.setLevels(levels);
        this.levelsTree.treeModel.update();
      }
      );
  }


  /***** Mark *****/
  // Filter results functions
  addPattern(): void {
    this.patterns.push(new SearchPatternModel());
  }

  removePattern(position: number): void {
    if (position < this.patterns.length - 1) { // Not last pattern
      this.patterns.splice(position, 1);
      if (this.patterns.length === 0) {
        this.addPattern();
      } else {
        this.searchByPatterns();
      }
    } else if (position === this.patterns.length - 1
      && this.patterns[position].searchValue !== '' && this.patterns[position].found < 0) { // Last pattern with search message and not searched
      this.patterns.splice(position, 1);
      this.addPattern();
    }
  }

  clearPatterns(): void {
    for (let pattern of this.patterns) {
      pattern.searchValue = '';
      pattern.results = [];
      pattern.found = -1;
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

  cleanRowsColor(rows?: HTMLElement[]): void {
    if (!rows) {
      rows = this.getSearchTableRows();
    }
    if (rows !== undefined && rows !== null) {
      let i: number = 0;
      while (rows[i] !== undefined && rows[i] !== null) {
        rows[i].removeAttribute('style');
        i++;
      }
    }
  }

  markOrClean(index: number): void {
    let pattern: SearchPatternModel = this.patterns[index];
    if (pattern.found < 1) {
      this.searchByPattern(index);
    } else {
      pattern.found = -1;
      pattern.position = -1;
      pattern.results = [];
      let searchValue: string = pattern.searchValue;
      pattern.searchValue = '';
      this.searchByPatterns();
      pattern.searchValue = searchValue;
    }
  }

  searchByPatterns(rows?: HTMLElement[]): void {
    if (!rows) {
      rows = this.getSearchTableRows();
    }
    this.currentPos = -1;
    this.cleanRowsColor(rows);
    let i: number = 0;
    this.logRows
      .map(
      (row: any) => {
        for (let pattern of this.patterns) {
          if (i === 0) { // First iteration of map
            pattern.results = []; // Initialize results to empty
          }
          if ((pattern.searchValue !== '') && (row.message.toUpperCase().indexOf(pattern.searchValue.toUpperCase()) > -1)) {
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
        this.next(j, rows);
        this.paintResults(j, rows);
      }
      j++;
    }
  }

  searchByPattern(position: number): void {
    if (this.patterns[position].searchValue !== '') {
      let rows: HTMLElement[] = this.getSearchTableRows();
      this.searchByPatterns(rows);
      this.currentPos = -1;
      this.next(position, rows);
      this.paintResults(position, rows);
      if (position === this.patterns.length - 1) {
        this.addPattern();
      }
    } else {
      this.elastestESService.popupService.openSnackBar('Search value can not be empty', 'OK');
    }
  }

  paintResults(index: number, rows?: HTMLElement[]): void {
    if (!rows) {
      rows = this.getSearchTableRows();
    }
    if (rows !== undefined && rows !== null) {
      for (let result of this.patterns[index].results) {
        if (rows[result]) {
          rows[result].style.color = this.patterns[index].color;
          console.log(rows[result], rows[result].style.color)
        }
      }
    }
  }

  next(index: number, rows?: HTMLElement[]): void {
    let pattern: SearchPatternModel = this.patterns[index];
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
      this.focusRow(pattern.results[pattern.position], rows);
    }
  }


  prev(index: number, rows?: HTMLElement[]): void {
    let pattern: SearchPatternModel = this.patterns[index];
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
      this.focusRow(pattern.results[pattern.position], rows);
    }
  }

  focusRow(newPos: number, rows?: HTMLElement[]): void {
    let previousPos: number = this.currentPos;
    this.currentPos = newPos;
    if (!rows) {
      rows = this.getSearchTableRows();
    }
    if (rows !== undefined && rows !== null) {
      if (previousPos >= 0) {
        rows[previousPos].style.removeProperty('background-color');
      }
      rows[this.currentPos].style.backgroundColor = '#e0e0e0';
      rows[this.currentPos].focus();
    }
  }

  getNextPosition(element: number, array: Array<number>): number {
    let i: number;
    for (i = 0; i < array.length; i++) {
      if (element < array[i]) {
        return i;
      }
    }
    return -1;
  }

  getPrevPosition(element: number, array: Array<number>): number {
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

  getSearchTableRows(): HTMLElement[] {
    let table: HTMLElement = document.getElementById('logsGrid');
    let rows: HTMLElement[] = [];
    if (table !== null && table !== undefined) {
      Array.from(table.getElementsByClassName('ag-row')).forEach(
        (node: Element) => rows.push(node as HTMLElement)
      );
      console.log(rows);
      return rows;
    } else {
      return undefined;
    }
  }

  openColorPicker(i: number) {
    document.getElementById('pattern' + i + 'Color').click();
  }
}
