import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplexMetricsViewComponent } from './complex-metrics-view.component';

describe('ComplexMetricsViewComponent', () => {
  let component: ComplexMetricsViewComponent;
  let fixture: ComponentFixture<ComplexMetricsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ComplexMetricsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ComplexMetricsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
