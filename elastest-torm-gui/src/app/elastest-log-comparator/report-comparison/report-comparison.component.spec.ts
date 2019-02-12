import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReportComparisonComponent} from './report-comparison.component';
import {RouterTestingModule} from '@angular/router/testing';
import {
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatStepperModule,
  MatTabsModule
} from '@angular/material';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {
  CovalentDataTableModule,
  CovalentFileModule,
  CovalentLoadingModule,
  CovalentMessageModule
} from '@covalent/core';
import {BreadcrumbsModule} from 'ng2-breadcrumbs';
import {FormsModule} from '@angular/forms';
import {NgbPopoverModule} from '@ng-bootstrap/ng-bootstrap';
import {TableService} from '../../../../service/table.service';
import {ElasticsearchService} from '../../../../service/elasticsearch.service';

describe('Component: Report Comparison', () => {
  let component: ReportComparisonComponent;
  let fixture: ComponentFixture<ReportComparisonComponent>;
  let elasticsearchService: ElasticsearchService;
  let tableService: TableService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, MatIconModule, MatCardModule, MatFormFieldModule, MatTabsModule, CovalentMessageModule,
        CovalentLoadingModule, CovalentFileModule, MatStepperModule, MatListModule, FormsModule, HttpClientTestingModule,
        BreadcrumbsModule, MatButtonModule, NgbPopoverModule, MatProgressSpinnerModule, CovalentDataTableModule],
      declarations: [ReportComparisonComponent],
      providers: [ElasticsearchService, TableService]
    });
    fixture = TestBed.createComponent(ReportComparisonComponent);
    component = fixture.componentInstance;
    elasticsearchService = TestBed.get(ElasticsearchService);
    tableService = TestBed.get(TableService);
  });

  it('Should create the component', () => {
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
});
