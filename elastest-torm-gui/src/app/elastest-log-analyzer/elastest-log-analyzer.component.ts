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
import { AfterViewInit, Component, ElementRef, OnChanges, OnInit, ViewChild, Input } from '@angular/core';
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
  Column,
} from 'ag-grid/main';
import { ITreeOptions, IActionMapping } from 'angular-tree-component';
import { TreeComponent } from 'angular-tree-component';
import { ShowMessageModalComponent } from './show-message-modal/show-message-modal.component';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';
import { MarkComponent } from './mark-component/mark.component';
import { TreeNode } from 'angular-tree-component/dist/defs/api';
import { TJobExecService } from '../elastest-etm/tjob-exec/tjobExec.service';
import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { TitlesService } from '../shared/services/titles.service';
import { ExternalService } from '../elastest-etm/external/external.service';
import { ExternalTJobExecModel } from '../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';

@Component({
  selector: 'elastest-log-analyzer',
  templateUrl: './elastest-log-analyzer.component.html',
  styleUrls: ['./elastest-log-analyzer.component.scss'],
})
export class ElastestLogAnalyzerComponent implements OnInit, AfterViewInit {
  public gridApi: GridApi;
  public gridColumnApi: ColumnApi;

  public esSearchModel: ESSearchModel;
  public logAnalyzer: LogAnalyzerModel;
  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();
  public filters: string[] = ['@timestamp', 'message', 'level', 'et_type', 'component', 'stream', 'stream_type', 'exec'];

  public logRows: any[] = [];
  public logColumns: any[] = [];
  public autoRowHeight: boolean = true;
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
    /*getRowHeight: function(params) {
      return 18 * (Math.floor(params.data.message.length / 30) + 1);
    },*/
  };

  private charsByLine: number = 0;

  @Input() tJobId: number;
  @Input() tJobExecId: number;
  @Input() testCase: string;
  @Input() isEmbed: boolean = false;
  @Input() componentStreams: any;
  @Input() exTJob: number;
  @Input() exTJobExec: number;
  @Input() exTestCase: number;
  @Input() exTestExec: number;

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

  // TestCase
  withTestCase: boolean = false;
  testCaseName: string = undefined;

  constructor(
    public dialog: MdDialog,
    public router: Router,
    private elastestESService: ElastestESService,
    private logAnalyzerService: LogAnalyzerService,
    private tJobExecService: TJobExecService,
    private titlesService: TitlesService,
    private externalService: ExternalService,
  ) {}

  ngOnInit() {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    this.logAnalyzer = new LogAnalyzerModel();
    this.initLogAnalyzer();
    this.logAnalyzerService.getLogAnalyzerConfig().subscribe(
      (logAnalyzerConfig: LogAnalyzerConfigModel) => {
        if (logAnalyzerConfig !== undefined) {
          this.logAnalyzer.laConfig = logAnalyzerConfig;
        }
      },
      // (error) => console.log(error),
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

  initLogAnalyzer(): void {
    let params: any = this.router.parseUrl(this.router.url).queryParams;
    let fromExec: any;
    if ((params.tjob && params.exec) || (this.tJobId !== undefined && this.tJobExecId !== undefined)) {
      fromExec = {
        type: 'normal',
        tJob: params.tjob ? params.tjob : this.tJobId ? this.tJobId : undefined,
        exec: params.exec ? params.exec : this.tJobExecId ? this.tJobExecId : undefined,
        testCase: params.testCase ? params.testCase : this.testCase ? this.testCase : undefined,
      };
    } else if ((params.exTJob && params.exTJobExec) || (this.exTJob !== undefined && this.exTJobExec !== undefined)) {
      fromExec = {
        type: 'external',
        exTJob: params.exTJob ? params.exTJob : this.exTJob ? this.exTJob : undefined,
        exTJobExec: params.exTJobExec ? params.exTJobExec : this.exTJobExec ? this.exTJobExec : undefined,
        exTestCase: params.exTestCase ? params.exTestCase : this.exTestCase ? this.exTestCase : undefined,
        exTestExec: params.exTestExec ? params.exTestExec : this.exTestExec ? this.exTestExec : undefined,
      };
    }
    if (!this.isEmbed) {
      this.openSelectExecutions(fromExec);
    } else {
      if (this.tJobId && this.tJobExecId) {
        this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe(
          (tjobExec: TJobExecModel) => {
            let data: { fromDate: Date; selectedIndices: string[]; toDate: Date } = {
              fromDate: tjobExec.startDate,
              selectedIndices: [tjobExec.monitoringIndex],
              toDate: tjobExec.endDate,
            };
            this.loadSelectExecutions(data, fromExec);
          },
          (error) => console.log(error),
        );
      } else if (this.exTJob && this.exTJobExec) {
        this.externalService.getExternalTJobExecById(this.exTJobExec).subscribe((exTJobExec: ExternalTJobExecModel) => {
          let data: { fromDate: Date; selectedIndices: string[]; toDate: Date } = {
            fromDate: exTJobExec.startDate,
            selectedIndices: [exTJobExec.monitoringIndex],
            toDate: exTJobExec.endDate,
          };
          this.loadSelectExecutions(data, fromExec);
        });
      }
    }
  }
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
    this.esSearchModel = this.initAndGetESModel();
  }

  initAndGetESModel(): ESSearchModel {
    let esSearchModel: ESSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    esSearchModel.body.boolQuery.bool.must.termList.push(this.streamTypeTerm);
    esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
    esSearchModel.body.sort.sortMap.set('_uid', 'asc'); // Sort by _id too to prevent traces of the same millisecond being disordered
    return esSearchModel;
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
    this.prepareLoadLogBasic();
    this.setTerms();
    this.setMatch();
  }
  prepareLoadLogBasic(): void {
    this.disableBtns = true;
    this.initESModel();

    this.logAnalyzer.fromDate = this.getFromDate();
    this.logAnalyzer.toDate = this.getToDate();

    this.esSearchModel.indices = this.logAnalyzer.selectedIndices;
    this.esSearchModel.filterPathList = this.filters;
    this.esSearchModel.body.size = this.logAnalyzer.maxResults;

    this.setRange();
  }

  setRange(): void {
    this.setRangeByGiven();
  }

  setRangeByGiven(
    from: Date | string = this.getFromDate(),
    to: Date | string = this.getToDate(),
    includedFrom: boolean = true,
    includedTo: boolean = true,
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

  setRangeByGivenToGivenEsSearchModel(): void {}

  setTerms(): void {
    if (!this.logAnalyzer.componentsStreams.empty()) {
      this.esSearchModel.body.boolQuery.bool.must.boolList.push(this.logAnalyzer.getComponentsStreamsBool());
    }

    if (!this.logAnalyzer.levels.empty()) {
      this.esSearchModel.body.boolQuery.bool.must.addTermListToTermList(this.logAnalyzer.getLevelsTermList());
    }
  }

  setMatch(msg: string = this.logAnalyzer.messageFilter): void {
    this.setMatchByGivenEsSearchModel(msg, this.esSearchModel);
  }

  setMatchByGivenEsSearchModel(msg: string = '', esSearchModel: ESSearchModel): void {
    /* Message field by default */
    if (msg !== '') {
      let messageMatch: ESMatchModel = new ESMatchModel();
      messageMatch.field = 'message';
      messageMatch.query = '*' + msg + '*';
      messageMatch.type = 'phrase_prefix';
      esSearchModel.body.boolQuery.bool.must.matchList.push(messageMatch);
    }
  }

  setTableHeader(): void {
    this.logColumns = [];

    for (let field of this.esSearchModel.filterPathList) {
      if (field !== 'stream_type' && field !== 'et_type') {
        // stream_type is always log
        let columnObj: AgGridColumn = new AgGridColumn();
        columnObj.headerName = field;
        columnObj.field = field;
        columnObj.width = 240;
        columnObj.suppressSizeToFit = false;
        columnObj.tooltipField = field;

        switch (field) {
          case '@timestamp':
            columnObj.maxWidth = 232;
            columnObj.hide = this.isEmbed;
            break;
          case 'message':
            break;
          case 'level':
            columnObj.maxWidth = 100;
            columnObj.hide = this.isEmbed;
            break;
          case 'component':
            columnObj.maxWidth = 260;
            break;
          case 'stream':
            columnObj.maxWidth = 188;
            columnObj.hide = this.isEmbed;
            break;
          case 'exec':
            columnObj.maxWidth = 60;
            columnObj.hide = this.isEmbed;
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

  loadLog(withoutPrepare: boolean = false): void {
    if (this.withTestCase && this.testCaseName) {
      this.filterTestCase(this.testCaseName);
      return;
    }
    this.logAnalyzer.usingTail = this.logAnalyzer.tail;
    this.logAnalyzer.stopTail();

    this.logAnalyzer.selectedRow = undefined;

    if (!withoutPrepare) {
      this.prepareLoadLog();
    }

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: object = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody).subscribe(
      (data: any) => {
        let logs: any[] = this.esSearchModel.getDataListFromRaw(data, false);
        this.loadLogByGivenData(logs);
      },
      (error) => {
        this.disableBtns = false;
      },
    );
  }

  loadLogByGivenData(data: any[] = []): void {
    this.logRows = data;
    let logsLoaded: boolean = this.logRows.length > 0;
    if (logsLoaded) {
      this.setTableHeader();
      this.mark.removeAllPatterns();
      if (!this.isEmbed) {
        this.popup('Logs has been loaded');
      }
    } else {
      if (!this.isEmbed) {
        this.popup("There aren't logs to load", 'OK');
      }
    }
    this.updateButtons(logsLoaded);
    if (this.logAnalyzer.usingTail) {
      this.loadTailLog(!logsLoaded);
    }
    this.disableBtns = false;
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
    let searchBody: object = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody).subscribe(
      (data: any) => {
        let moreRows: any[] = this.esSearchModel.getDataListFromRaw(data, false);
        if (moreRows.length > 0) {
          this.logRows = this.logRows.concat(moreRows);
          this.popup('Loaded more logs');
          this.setTableHeader();
          this.updateButtons(!fromTail);
          this.mark.searchByPatterns();
        } else {
          this.popup("There aren't more logs to load", 'OK');
          this.disableLoadMore = true; // removed from html temporally
        }
        this.disableBtns = false;
      },
      (error) => {
        this.disableBtns = false;
      },
    );
  }

  moreFromSelected(): void {
    if (this.logAnalyzer.hasSelectedRow()) {
      let selected: number = this.logAnalyzer.selectedRow;
      if (selected === this.logRows.length - 1) {
        // If selected is last
        this.loadMore();
      } else {
        this.prepareLoadLog();
        let from: Date = this.logRows[selected]['@timestamp'];
        let to: Date = this.logRows[selected + 1]['@timestamp'];
        this.setRangeByGiven(from, to, true, false);

        this.esSearchModel.body.searchAfter = this.logRows[selected].sort;
        let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
        let searchBody: object = this.esSearchModel.getSearchBody();
        this.elastestESService.search(searchUrl, searchBody).subscribe(
          (data: any) => {
            let moreRows: any[] = this.esSearchModel.getDataListFromRaw(data, false);

            if (moreRows.length > 0) {
              this.insertRowsFromPosition(selected, moreRows);

              this.popup('Loaded more logs from selected trace');
              this.setTableHeader();
              this.updateButtons();
              this.mark.searchByPatterns();
            } else {
              this.popup("There aren't logs to load or you don't change filters", 'OK');
            }
            this.disableBtns = false;
          },
          (error) => {
            this.disableBtns = false;
          },
        );
      }
    } else {
      this.popup("There isn't trace selected. Please, do click on a row", 'OK');
    }
  }

  insertRowsFromPosition(pos: number, rows: any[]) {
    let firstHalf: any[] = this.logRows.slice(0, pos + 1);
    let secondHalf: any[] = this.logRows.slice(pos + 1);

    this.logRows = firstHalf.concat(rows).concat(secondHalf);
  }

  /***************************/
  /***** Grid and Events *****/
  /***************************/

  public componentStateChanged($event: ComponentStateChangedEvent): void {
    // On changes detected
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
      height: '90%',
      width: '80%',
    });
  }

  public refreshView(): void {
    if (this.gridApi) {
      this.gridApi.redrawRows();
    }
  }

  toggleRowHeight(): void {
    if (!this.autoRowHeight) {
      let messageColumn: any = this.gridOptions.columnApi.getAllDisplayedColumns().filter((column: Column) => {
        return column.getColId() === 'message';
      })[0];
      if (messageColumn && messageColumn.actualWidth) {
        this.setRowHeight(messageColumn.actualWidth);
      }
    } else {
      this.gridOptions.api.resetRowHeights();
    }
  }

  public setRowHeight(columnWidth: number): void {
    this.charsByLine = Math.trunc((columnWidth - 13) / 7.85);
    this.gridOptions.api.forEachNode((rowNode: RowNode) => {
      let height: number = 20 * Math.ceil(rowNode.data.message.length / this.charsByLine);
      height < 20 ? (height = 20) : (height = height);
      rowNode.setRowHeight(height);
    });
    this.gridOptions.api.onRowHeightChanged();
  }

  public saveColumnsConfig(event: any, showPopup: boolean = true, persist: boolean = false): void {
    if (this.autoRowHeight && event && event.column.colId === 'message' && event.finished) {
      this.setRowHeight(event.column.actualWidth);
    }
    this.logAnalyzer.laConfig.columnsState = this.gridColumnApi.getColumnState();
    if (persist) {
      this.logAnalyzerService.saveLogAnalyzerConfig(this.logAnalyzer.laConfig).subscribe(
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
    this.logAnalyzerService.getLogAnalyzerConfig().subscribe(
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
      },
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

  public setWithTestCase(testCase: boolean): void {
    this.withTestCase = testCase;
  }

  /**** Modal ****/
  public openSelectExecutions(fromExec?: any): void {
    let dialogRef: MdDialogRef<GetIndexModalComponent> = this.dialog.open(GetIndexModalComponent, {
      data: fromExec,
      height: '90%',
      width: '80%',
    });
    dialogRef.afterClosed().subscribe((data: any) => {
      this.loadSelectExecutions(data, fromExec);
    });
  }

  loadSelectExecutions(data: any, fromExec: any): void {
    if (data) {
      // Ok Pressed
      if (data.selectedIndices.length > 0 && data.selectedIndices !== '') {
        this.logAnalyzer.selectedIndices = data.selectedIndices;
        if (data.fromDate) {
          this.setFromDate(data.fromDate);
        }
        if (data.toDate) {
          this.setToDate(data.toDate);
        }
        if (this.isEmbed && this.componentStreams) {
          this.logAnalyzer.setComponentsStreams(this.componentStreams);
          this.componentsTree.treeModel.update();
        } else {
          this.loadComponentStreams();
        }
        this.loadLevels();
        this.withTestCase = fromExec && (fromExec.testCase || fromExec.exTestCase);
        if (this.withTestCase) {
          if (fromExec.testCase) {
            this.testCaseName = fromExec.testCase;
          } else {
            this.testCaseName = fromExec.exTestCase;
          }
          this.filterTestCase(this.testCaseName);
        } else {
          this.loadLog();
        }
      } else {
        this.popup('No execution was selected. Selected all by default');
        this.loadLog();
      }
    } else {
    }
  }

  filterTestCase(testCase: string): void {
    let startMsg: string = '##### Start test: ' + testCase;
    let endMsg: string = '##### Finish test: ' + testCase;

    // Search Start Msg
    this.searchTraceByGivenMsg(startMsg).subscribe(
      (startData: any) => {
        startData = this.esSearchModel.getDataListFromRaw(startData, false);
        if (startData.length > 0) {
          let startRow: any = startData[0];
          this.setFromDate(new Date(startRow['@timestamp']));
          // Search Finish Msg
          this.searchTraceByGivenMsg(endMsg).subscribe(
            (finishData: any) => {
              finishData = this.esSearchModel.getDataListFromRaw(finishData, false);
              if (finishData.length > 0) {
                let finishRow: any = finishData[0];
                this.setToDate(new Date(finishRow['@timestamp']));
                let finishRowFullMsg: string = finishRow.message;
                this.esSearchModel.body.searchAfter = startRow.sort;
                this.setRangeByGiven(startRow['@timestamp'], finishRow['@timestamp']);
                // Load Logs
                this.logAnalyzer.selectedRow = undefined;
                let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
                let searchBody: object = this.esSearchModel.getSearchBody();

                this.elastestESService.search(searchUrl, searchBody).subscribe(
                  (data: any) => {
                    let logs: any[] = this.esSearchModel.getDataListFromRaw(data, false);

                    let finishObj: any = logs.find((x: any) => x.message === finishRowFullMsg);
                    if (finishObj) {
                      let finishIndex: number = logs.indexOf(finishObj);
                      logs.splice(finishIndex);
                    }

                    this.loadLogByGivenData(logs);
                  },
                  (error) => {
                    this.startBehaviourOnNoLogs();
                  },
                );
              } else {
                this.startBehaviourOnNoLogs();
              }
            },
            (error) => {
              this.startBehaviourOnNoLogs();
            },
          );
        } else {
          this.startBehaviourOnNoLogs();
        }
      },
      (error) => {
        this.startBehaviourOnNoLogs();
      },
    );
    // TODO search start MSG with  to make searchafter...
  }

  startBehaviourOnNoLogs(): void {
    this.loadLogByGivenData([]);
  }

  searchTraceByGivenMsg(msg: string): Observable<any> {
    this.prepareLoadLogBasic();

    this.setMatch(msg);

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: object = this.esSearchModel.getSearchBody();

    // Remove Match Filter
    this.esSearchModel.body.boolQuery.bool.must.matchList.pop();
    this.prepareLoadLog();

    return this.elastestESService.search(searchUrl, searchBody);
  }

  loadComponentStreams(): void {
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    componentStreamQuery.bool.must.termList.push(this.streamTypeTerm);

    let fieldsList: string[] = ['component', 'stream'];
    this.elastestESService
      .getAggTreeOfIndex(this.logAnalyzer.selectedIndicesToString(), fieldsList, componentStreamQuery.convertToESFormat())
      .subscribe((componentsStreams: any[]) => {
        let components: any[] = componentsStreams;
        if (this.isEmbed && this.exTJob !== undefined && this.exTJobExec !== undefined) {
          components = componentsStreams.filter((component) => {
            return component.name !== 'test';
          });
        }
        this.logAnalyzer.setComponentsStreams(components);
        this.componentsTree.treeModel.update();
      });
  }

  loadLevels(): void {
    let levelsQuery: ESBoolQueryModel = new ESBoolQueryModel();
    levelsQuery.bool.must.termList.push(this.streamTypeTerm);

    this.elastestESService
      .getAggTreeOfIndex(this.logAnalyzer.selectedIndicesToString(), ['level'], levelsQuery.convertToESFormat())
      .subscribe((levels: any[]) => {
        this.logAnalyzer.setLevels(levels);
        this.levelsTree.treeModel.update();
      });
  }
}
