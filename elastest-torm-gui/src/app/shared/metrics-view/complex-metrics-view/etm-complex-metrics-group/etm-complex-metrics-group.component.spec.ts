import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmComplexMetricsGroupComponent } from './etm-complex-metrics-group.component';

describe('EtmComplexMetricsGroupComponent', () => {
  let component: EtmComplexMetricsGroupComponent;
  let fixture: ComponentFixture<EtmComplexMetricsGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmComplexMetricsGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmComplexMetricsGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
