import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricsChartCardComponent } from './metrics-chart-card.component';

describe('MetricsChartCardComponent', () => {
  let component: MetricsChartCardComponent;
  let fixture: ComponentFixture<MetricsChartCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricsChartCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsChartCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
