import { LogAnalyzerService } from './log-analyzer.service';
import { AgGridColumn } from 'ag-grid-angular/main';
import { Router } from '@angular/router';
import { ESBoolQueryModel, ESMatchModel } from '../shared/elasticsearch-model/es-query-model';
import { ESRangeModel, ESTermModel } from '../shared/elasticsearch-model/es-query-model';
import { Observable, Subscription } from 'rxjs/Rx';
import { RowClickedEvent, RowDataChangedEvent, RowSelectedEvent, RowDoubleClickedEvent } from 'ag-grid/dist/lib/events';
import { LogAnalyzerModel } from './log-analyzer-model';
import { GetIndexModalComponent } from '../elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestESService } from '../shared/services/elastest-es.service';
import { ESSearchModel } from '../shared/elasticsearch-model/elasticsearch-model';
import { AfterViewInit, Component, ElementRef, OnChanges, OnInit, ViewChild } from '@angular/core';
import { dateToInputLiteral, invertColor } from './utils/Utils';
import { MdDialog, MdDialogRef } from '@angular/material';
import {
  CellClickedEvent,
  ColumnApi,
  ComponentStateChangedEvent,
  GridApi,
  GridOptions,
  GridReadyEvent,
  RowNode,
} from 'ag-grid/main';
import { ITreeOptions, IActionMapping } from 'angular-tree-component';
import { TreeComponent } from 'angular-tree-component';
import { ShowMessageModalComponent } from './show-message-modal/show-message-modal.component';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';
import { MarkComponent } from './mark-component/mark.component';

@Component({
  selector: 'elastest-log-analyzer',
  templateUrl: './elastest-log-analyzer.component.html',
  styleUrls: ['./elastest-log-analyzer.component.scss']
})
export class ElastestLogAnalyzerComponent implements OnInit, AfterViewInit {
  public gridApi: GridApi;
  public gridColumnApi: ColumnApi;

  public esSearchModel: ESSearchModel;
  public logAnalyzer: LogAnalyzerModel;
  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();

  public filters: string[] = ['@timestamp', 'message', 'level', 'type', 'component', 'stream', 'stream_type', 'exec'];

  public logRows: any[] = [];
  public logColumns: any[] = [];
  public gridOptions: GridOptions = {
    rowHeight: 22,
    headerHeight: 42,
    rowSelection: 'single',
    suppressRowClickSelection: false,
    suppressCellSelection: false, // Only supress key navigation and focus
    suppressChangeDetection: true,
    rowModelType: 'inMemory',
    suppressDragLeaveHidesColumns: true,
    enableCellChangeFlash: true,
    getRowStyle: this.setRowsStyle,
    // getRowHeight: function (params) {
    //   return 18 * (Math.floor(params.data.message.length / 45) + 1);
    // },
  };

  @ViewChild('fromDate') fromDate: ElementRef;
  @ViewChild('toDate') toDate: ElementRef;
  @ViewChild('mark') mark: MarkComponent;


  // Buttons
  public showLoadMore: boolean = false;
  public disableLoadMore: boolean = false;
  public showPauseTail: boolean = false;

  disableBtns: boolean = false;

  // Filters
  @ViewChild('componentsTree') componentsTree: TreeComponent;
  @ViewChild('levelsTree') levelsTree: TreeComponent;

  constructor(
    public dialog: MdDialog,
    public router: Router,
    private elastestESService: ElastestESService,
    private logAnalyzerService: LogAnalyzerService,
  ) {
    let params: any = router.parseUrl(router.url).queryParams;
    let fromExec: any;
    if (params.tjob && params.exec) {
      fromExec = {
        tJob: params.tjob,
        exec: params.exec
      };
    }

    this.openSelectExecutions(fromExec);
  }

  ngOnInit() {
    this.logAnalyzer = new LogAnalyzerModel();
    this.logAnalyzerService.getLogAnalyzerConfig()
      .subscribe(
      (logAnalyzerConfig: LogAnalyzerConfigModel) => {
        if (logAnalyzerConfig !== undefined) {
          this.logAnalyzer.laConfig = logAnalyzerConfig;
        }
      },
      (error) => console.log(error),
    );
    this.initStreamTypeTerm();
    this.initESModel();
  }

  ngAfterViewInit() {
    this.fromDate.nativeElement.value = this.getDefaultFromValue();
    this.toDate.nativeElement.value = this.getDefaultToValue();
  }

  ngOnDestroy() {
    this.logAnalyzer.stopTail();
  }

  /***** INIT *****/

  // Function to init some parameters of ag-grid
  onGridReady(params: GridReadyEvent | ComponentStateChangedEvent): void {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;

    if (this.logAnalyzer.laConfig.columnsState) {
      this.loadColumnsConfig(false);
    } else {
      this.gridApi.sizeColumnsToFit(); // State is saved automatically
    }
  }

  initStreamTypeTerm(): void {
    this.streamTypeTerm.name = 'stream_type';
    this.streamTypeTerm.value = this.streamType;
  }

  initESModel(): void {
    this.esSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    this.esSearchModel.body.boolQuery.bool.must.termList.push(this.streamTypeTerm);
    this.esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
    this.esSearchModel.body.sort.sortMap.set('_uid', 'asc'); // Sort by _id too to prevent traces of the same millisecond being disordered
  }

  /**********************/
  /***** Load Utils *****/
  /**********************/

  popup(msg: string, buttonMsg: string = 'OK'): void {
    let popupDuration: number = this.logAnalyzer.usingTail ? 1 : undefined;
    let popupCss: any[] = this.logAnalyzer.usingTail ? ['snackBarHidden'] : [];

    this.elastestESService.popupService.openSnackBar(msg, buttonMsg, popupDuration, popupCss);
  }

  prepareLoadLog(): void {
    this.disableBtns = true;
    this.initESModel();

    this.logAnalyzer.fromDate = this.getFromDate();
    this.logAnalyzer.toDate = this.getToDate();

    this.esSearchModel.indices = this.logAnalyzer.selectedIndices;
    this.esSearchModel.filterPathList = this.filters;
    this.esSearchModel.body.size = this.logAnalyzer.maxResults;
    this.setRange();
    this.setTerms();
    this.setMatch();
  }

  setRange(): void {
    this.setRangeByGiven();
  }

  setRangeByGiven(
    from: Date | string = this.getFromDate(), to: Date | string = this.getToDate(), includedFrom: boolean = true, includedTo: boolean = true
  ): void {
    this.esSearchModel.body.boolQuery.bool.must.range = new ESRangeModel();
    this.esSearchModel.body.boolQuery.bool.must.range.field = '@timestamp';

    if (includedFrom) {
      this.esSearchModel.body.boolQuery.bool.must.range.gte = from;
    } else {
      this.esSearchModel.body.boolQuery.bool.must.range.gt = from;
    }

    if (includedTo) {
      this.esSearchModel.body.boolQuery.bool.must.range.lte = to;
    } else {
      this.esSearchModel.body.boolQuery.bool.must.range.lt = to;
    }
  }

  setTerms(): void {
    if (!this.logAnalyzer.componentsStreams.empty()) {
      this.esSearchModel.body.boolQuery.bool.must.boolList.push(this.logAnalyzer.getComponentsStreamsBool());
    }

    if (!this.logAnalyzer.levels.empty()) {
      this.esSearchModel.body.boolQuery.bool.must.addTermListToTermList(this.logAnalyzer.getLevelsTermList());

    }
  }

  setMatch(): void {
    if (this.logAnalyzer.messageFilter !== '') {
      let messageMatch: ESMatchModel = new ESMatchModel();
      messageMatch.field = 'message';
      messageMatch.query = '*' + this.logAnalyzer.messageFilter + '*';
      messageMatch.type = 'phrase_prefix';
      this.esSearchModel.body.boolQuery.bool.must.matchList.push(messageMatch);
    }
  }

  setTableHeader(): void {
    this.logColumns = [];

    for (let field of this.esSearchModel.filterPathList) {
      if (field !== 'stream_type' && field !== 'type') { // stream_type is always log
        let columnObj: AgGridColumn = new AgGridColumn();
        columnObj.headerName = field;
        columnObj.field = field;
        columnObj.width = 240;
        columnObj.suppressSizeToFit = false;
        columnObj.tooltipField = field;

        switch (field) {
          case '@timestamp':
            columnObj.maxWidth = 232;
            break;
          case 'message':
            break;
          case 'level':
            columnObj.maxWidth = 100;
            break;
          case 'component':
            columnObj.maxWidth = 260;
            break;
          case 'stream':
            columnObj.maxWidth = 188;
            break;
          case 'exec':
            columnObj.maxWidth = 60;
            break;
          default:
            break;
        }

        this.logColumns.push(columnObj);
      }
    }
  }

  updateButtons(show: boolean = true): void {
    this.showLoadMore = show;
    this.disableLoadMore = !show;
    this.showPauseTail = !show;
  }

  /**************************/
  /***** Load functions *****/
  /**************************/

  verifyAndLoadLog($event, loadLog: boolean) {
    $event.preventDefault(); // On enter key pressed always opens modal. Prevent this
    if (loadLog) {
      this.loadLog();
    } else {
      this.popup('The search can not be performed. Please complete all required fields');
    }
  }

  loadLog(): void {
    this.logAnalyzer.usingTail = this.logAnalyzer.tail;
    this.logAnalyzer.stopTail();

    this.logAnalyzer.selectedRow = undefined;
    this.prepareLoadLog();

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: string = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody)
      .subscribe(
      (data: any) => {
        this.logRows = this.esSearchModel.getDataListFromRaw(data, false);

        let logsLoaded: boolean = this.logRows.length > 0;
        if (logsLoaded) {
          this.setTableHeader();
          this.mark.removeAllPatterns();
          this.popup('Logs has been loaded');
        } else {
          this.popup('There aren\'t logs to load', 'OK');
        }
        this.updateButtons(logsLoaded);
        if (this.logAnalyzer.usingTail) {
          this.loadTailLog(!logsLoaded);
        }
        this.disableBtns = false;
      },
      (error) => {
        this.disableBtns = false;
      },
    );
  }

  loadTailLog(notLoadedPrevious: boolean = false): void {
    let timer: Observable<number>;
    this.updateButtons(false);

    timer = Observable.interval(3500);
    this.logAnalyzer.tailSubscription = timer.subscribe(() => {
      if (!this.logAnalyzer.pauseTail) {
        this.loadMore(true);
      }
    });
  }

  loadMore(fromTail: boolean = false): void {
    this.prepareLoadLog();
    let lastTrace: any = this.logRows[this.logRows.length - 1];
    this.esSearchModel.body.searchAfter = lastTrace.sort;
    if (fromTail) {
      this.setRangeByGiven(lastTrace['@timestamp'], 'now');
      this.esSearchModel.body.size = 100;
    }

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: string = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody)
      .subscribe(
      (data: any) => {
        let moreRows: any[] = this.esSearchModel.getDataListFromRaw(data, false);
        if (moreRows.length > 0) {
          this.logRows = this.logRows.concat(moreRows);
          this.popup('Loaded more logs');
          this.setTableHeader();
          this.updateButtons(!fromTail);
          this.mark.searchByPatterns();
        } else {
          this.popup('There aren\'t more logs to load', 'OK');
          this.disableLoadMore = true; // removed from html temporally
        }
        this.disableBtns = false;
      },
      (error) => {
        this.disableBtns = false;
      }
      );
  }

  moreFromSelected(): void {
    if (this.logAnalyzer.hasSelectedRow()) {
      let selected: number = this.logAnalyzer.selectedRow;
      if (selected === this.logRows.length - 1) { // If selected is last
        this.loadMore();
      } else {
        this.prepareLoadLog();
        let from: Date = this.logRows[selected]['@timestamp'];
        let to: Date = this.logRows[selected + 1]['@timestamp'];
        this.setRangeByGiven(from, to, true, false);

        this.esSearchModel.body.searchAfter = this.logRows[selected].sort;
        let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
        let searchBody: string = this.esSearchModel.getSearchBody();
        this.elastestESService.search(searchUrl, searchBody)
          .subscribe(
          (data: any) => {
            let moreRows: any[] = this.esSearchModel.getDataListFromRaw(data, false);

            if (moreRows.length > 0) {
              this.insertRowsFromPosition(selected, moreRows);

              this.popup('Loaded more logs from selected trace');
              this.setTableHeader();
              this.updateButtons();
              this.mark.searchByPatterns();
            } else {
              this.popup('There aren\'t logs to load or you don\'t change filters', 'OK');
            }
            this.disableBtns = false;
          },
          (error) => {
            this.disableBtns = false;
          }
          );
      }
    } else {
      this.popup('There isn\'t trace selected. Please, do click on a row', 'OK');
    }
  }

  insertRowsFromPosition(pos: number, rows: any[]) {
    let firstHalf: any[] = this.logRows.slice(0, pos + 1);
    let secondHalf: any[] = this.logRows.slice(pos + 1);

    this.logRows = (firstHalf.concat(rows)).concat(secondHalf);
  }

  /***************************/
  /***** Grid and Events *****/
  /***************************/

  public componentStateChanged($event: ComponentStateChangedEvent): void { // On changes detected
    this.onGridReady($event);
    this.mark.model = this;
    if (this.logAnalyzer.usingTail) {
      $event.api.ensureIndexVisible(this.logRows.length - 1, 'undefined');
    }
  }

  public setRowsStyle(params: any): any {
    let style: any;
    let init: boolean = false;
    if (params.data.marked) {
      if (!init) {
        init = true;
        style = {};
      }
      style.background = params.data.marked;
      style.color = invertColor(params.data.marked, true);
    }
    if (params.data.focused) {
      if (!init) {
        init = true;
        style = {};
      }
      style.background = '#666666';
      style.color = invertColor(style.background, true);
    }
    return style;
  }

  public switchRowSelection($event: RowSelectedEvent): void {
    let row: RowNode = $event.node;
    if (row.isSelected()) {
      this.logAnalyzer.selectedRow = $event.rowIndex;
    } else {
      if (this.logAnalyzer.selectedRow === $event.rowIndex) {
        this.logAnalyzer.selectedRow = undefined;
      }
    }
  }
  public openMessageModal($event: RowDoubleClickedEvent): void {
    let dialogRef: MdDialogRef<ShowMessageModalComponent> = this.dialog.open(ShowMessageModalComponent, {
      data: { row: $event.data, columns: this.logColumns },
      height: '80%',
      width: '90%',
    });
  }

  public refreshView(): void {
    if (this.gridApi) {
      this.gridApi.redrawRows();
    }
  }

  public saveColumnsConfig(showPopup: boolean = true, persist: boolean = false): void {
    this.logAnalyzer.laConfig.columnsState = this.gridColumnApi.getColumnState();
    if (persist) {
      this.logAnalyzerService.saveLogAnalyzerConfig(this.logAnalyzer.laConfig)
        .subscribe(
        (logAnalyzerConfig: LogAnalyzerConfigModel) => {
          this.logAnalyzer.laConfig = logAnalyzerConfig;
        },
        (error) => {
          this.popup('An error occurred while trying to save the configuration');
          console.log('Error on save LogAnalyzer column configuration:', error);
        },
      );
    }
    if (showPopup) {
      this.popup('Columns configuration has been saved');
    }
  }

  public loadColumnsConfig(showPopup: boolean = true): void {
    if (this.logAnalyzer.laConfig.columnsState) {
      this.gridColumnApi.setColumnState(this.logAnalyzer.laConfig.columnsState);
      if (showPopup) {
        this.popup('Saved columns configuration has been loaded');
      }
    }
  }

  public loadSavedColumnsConfig(): void {
    this.logAnalyzerService.getLogAnalyzerConfig()
      .subscribe(
      (logAnalyzerConfig: LogAnalyzerConfigModel) => {
        if (logAnalyzerConfig !== undefined) {
          this.logAnalyzer.laConfig = logAnalyzerConfig;
          this.loadColumnsConfig(false);
        } else {
          this.popup('There is not any saved configuration to load');
        }
      },
      (error) => {
        this.popup('Error on load saved configuration');
        console.log(error);
      }
      );
  }

  public loadDefaultColumnsConfig(): void {
    this.logAnalyzer.laConfig = new LogAnalyzerConfigModel();
    this.setTableHeader();
    this.popup('Default columns configuration has been loaded');
  }

  /**********************/
  /***** Date utils *****/
  /**********************/

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


  /**** Modal ****/
  public openSelectExecutions(fromExec?: any): void {
    let dialogRef: MdDialogRef<GetIndexModalComponent> = this.dialog.open(GetIndexModalComponent, {
      data: fromExec,
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
            if (data.toDate) {
              this.setToDate(data.toDate);
            }
            this.loadComponentStreams();
            this.loadLevels();
          } else {
            this.popup('No execution was selected. Selected all by default');
          }
          this.loadLog();
        } else { }
      },
    );
  }

  loadComponentStreams(): void {
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    componentStreamQuery.bool.must.termList.push(this.streamTypeTerm);

    let fieldsList: string[] = ['component', 'stream'];
    this.elastestESService.getAggTreeOfIndex(
      this.logAnalyzer.selectedIndicesToString(), fieldsList, componentStreamQuery.convertToESFormat()
    ).subscribe(
      (componentsStreams: any[]) => {
        this.logAnalyzer.setComponentsStreams(componentsStreams);
        this.componentsTree.treeModel.update();
      }
      );
  }

  loadLevels(): void {
    let levelsQuery: ESBoolQueryModel = new ESBoolQueryModel();
    levelsQuery.bool.must.termList.push(this.streamTypeTerm);

    this.elastestESService.getAggTreeOfIndex(
      this.logAnalyzer.selectedIndicesToString(), ['level'], levelsQuery.convertToESFormat()
    ).subscribe(
      (levels: any[]) => {
        this.logAnalyzer.setLevels(levels);
        this.levelsTree.treeModel.update();
      }
      );
  }

}