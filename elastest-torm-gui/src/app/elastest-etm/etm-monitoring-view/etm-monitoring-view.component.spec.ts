import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmMonitoringViewComponent } from './etm-monitoring-view.component';

describe('EtmMonitoringViewComponent', () => {
  let component: EtmMonitoringViewComponent;
  let fixture: ComponentFixture<EtmMonitoringViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmMonitoringViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmMonitoringViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
