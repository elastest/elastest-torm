import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalMonitoringDbComponent } from './external-monitoring-db.component';

describe('ExternalMonitoringDbComponent', () => {
  let component: ExternalMonitoringDbComponent;
  let fixture: ComponentFixture<ExternalMonitoringDbComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalMonitoringDbComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalMonitoringDbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
