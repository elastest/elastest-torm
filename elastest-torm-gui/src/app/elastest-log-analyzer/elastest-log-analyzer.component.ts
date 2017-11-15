import { LogAnalyzerModel } from './log-analyzer-model';
import { GetIndexModalComponent } from '../elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestESService } from '../shared/services/elastest-es.service';
import { ESSearchModel, ESTermModel } from '../shared/elasticsearch-model';
import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { dateToInputLiteral } from './utils/Utils';
import { MdDialog, MdDialogRef } from '@angular/material';

@Component({
  selector: 'app-elastest-log-analyzer',
  templateUrl: './elastest-log-analyzer.component.html',
  styleUrls: ['./elastest-log-analyzer.component.scss']
})
export class ElastestLogAnalyzerComponent implements OnInit, AfterViewInit {
  public esSearchModel: ESSearchModel;
  public logAnalyzerModel: LogAnalyzerModel;
  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();

  public logRows: any[] = [];
  public logColumns: any[] = [];

  @ViewChild('fromDate') fromDate: ElementRef;
  @ViewChild('toDate') toDate: ElementRef;

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

  initStreamTypeTerm(): void {
    this.streamTypeTerm.name = 'stream_type';
    this.streamTypeTerm.value = this.streamType;
  }

  initESModel(): void {
    this.esSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    this.esSearchModel.body.query.bool.must.termList.push(this.streamTypeTerm);
  }

  loadLog(): void {
    this.logAnalyzerModel.fromDate = this.fromDate.nativeElement.value;
    this.logAnalyzerModel.toDate = this.toDate.nativeElement.value;

    this.esSearchModel.indices = this.logAnalyzerModel.selectedIndices;
    this.esSearchModel.filterPathList = this.elastestESService.getBasicFilterFields(this.streamType);
    this.esSearchModel.body.size = this.logAnalyzerModel.maxResults;

    let searchUrl: string = this.esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: string = this.esSearchModel.getSearchBody();

    this.elastestESService.search(searchUrl, searchBody)
      .subscribe(
      (data: any) => {
        this.logRows = this.esSearchModel.getDataListFromRaw(data);
        console.log(this.logRows);

        if (this.logRows.length > 0) {
          this.elastestESService.popupService.openSnackBar('Logs has been loaded');
          this.setTableHeader();
        } else {
          this.elastestESService.popupService.openSnackBar('There aren\'t logs to load', 'OK');
        }

      }
      );
  }

  setTableHeader(): void {
    this.logColumns = [];

    for (let field of this.esSearchModel.filterPathList) {
      this.logColumns.push(
        { headerName: field, field: field, width: 300 },
      );
    }
  }



  /**** Dates ****/

  public getDefaultFromValue(): string {
    return dateToInputLiteral(this.logAnalyzerModel.getDefaultFromDate());
  }

  public getDefaultToValue(): string {
    return dateToInputLiteral(this.logAnalyzerModel.getDefaultToDate());
  }

  public setUseTail(tail: boolean): void {
    this.logAnalyzerModel.tail = tail;
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
