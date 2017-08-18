import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmLogsMetricsViewComponent } from './etm-logs-metrics-view.component';

describe('EtmLogsMetricsViewComponent', () => {
  let component: EtmLogsMetricsViewComponent;
  let fixture: ComponentFixture<EtmLogsMetricsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmLogsMetricsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmLogsMetricsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
