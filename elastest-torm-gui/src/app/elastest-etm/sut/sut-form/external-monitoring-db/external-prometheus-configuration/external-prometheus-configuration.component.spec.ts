import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalPrometheusConfigurationComponent } from './external-prometheus-configuration.component';

describe('ExternalPrometheusConfigurationComponent', () => {
  let component: ExternalPrometheusConfigurationComponent;
  let fixture: ComponentFixture<ExternalPrometheusConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalPrometheusConfigurationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalPrometheusConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
