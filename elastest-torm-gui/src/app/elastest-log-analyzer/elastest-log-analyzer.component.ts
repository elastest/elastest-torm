import { LogAnalyzerModel } from './log-analyzer-model';
import { GetIndexModalComponent } from '../elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestESService } from '../shared/services/elastest-es.service';
import { ESSearchModel, ESTermModel } from '../shared/elasticsearch-model';
import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { dateToInputLiteral } from './utils/Utils';
import { MdDialog, MdDialogRef } from '@angular/material';
import { ColumnApi, GridApi } from 'ag-grid/main';

@Component({
  selector: 'elastest-log-analyzer',
  templateUrl: './elastest-log-analyzer.component.html',
  styleUrls: ['./elastest-log-analyzer.component.scss']
})
export class ElastestLogAnalyzerComponent implements OnInit, AfterViewInit {
  private gridApi: GridApi;
  private gridColumnApi: ColumnApi;

  public esSearchModel: ESSearchModel;
  public logAnalyzerModel: LogAnalyzerModel;
  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();

  public logRows: any[] = [];
  public logColumns: any[] = [];

  @ViewChild('fromDate') fromDate: ElementRef;
  @ViewChild('toDate') toDate: ElementRef;

  // Buttons
  public showLoadMore: boolean = false;
  public showPauseTail: boolean = false;
  public showClearData: boolean = false;

  constructor(
    public dialog: MdDialog, private elastestESService: ElastestESService,
  ) {
  }

  ngOnInit() {
    this.logAnalyzerModel = new LogAnalyzerModel();
    this.initStreamTypeTerm();
    this.initESModel();
  }

  ngAfterViewInit() {
    this.fromDate.nativeElement.value = this.getDefaultFromValue();
    this.toDate.nativeElement.value = this.getDefaultToValue();
  }

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

  loadLog(): void {
    this.logAnalyzerModel.fromDate = this.fromDate.nativeElement.value;
    this.logAnalyzerModel.toDate = this.toDate.nativeElement.value;

    this.esSearchModel.indices = this.logAnalyzerModel.selectedIndices;
    this.esSearchModel.filterPathList = this.elastestESService.getBasicFilterFields(this.streamType);
    this.esSearchModel.body.size = this.logAnalyzerModel.maxResults;
    this.setRange();

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: string = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody)
      .subscribe(
      (data: any) => {
        this.logRows = this.esSearchModel.getDataListFromRaw(data);
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

  setTableHeader(): void {
    this.logColumns = [];

    for (let field of this.esSearchModel.filterPathList) {
      if (field !== 'stream_type') { // stream_type is always log
        this.logColumns.push(
          { headerName: field, field: field, width: 280, suppressSizeToFit: false, },
        );
      }
    }
  }


  updateButtons(show: boolean): void {
    this.showLoadMore = show;
    this.showClearData = show;
  }
  /**** Dates ****/

  public getDefaultFromValue(): string {
    return dateToInputLiteral(this.logAnalyzerModel.getDefaultFromDate());
  }

  public getDefaultToValue(): string {
    return dateToInputLiteral(this.logAnalyzerModel.getDefaultToDate());
  }

  public getFromDate(): any {
    return this.fromDate.nativeElement.value;
  }

  public getToDate(): any {
    return this.toDate.nativeElement.value;
  }

  public setUseTail(tail: boolean): void {
    this.logAnalyzerModel.tail = tail;
  }

  public clearData() {
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
        if (data && data.selectedIndices && data.selectedIndices !== '') {
          this.logAnalyzerModel.selectedIndices = data.selectedIndices;
        } else {
          this.elastestESService.popupService.openSnackBar('No execution was selected');
        }
      },
    );
  }
}
