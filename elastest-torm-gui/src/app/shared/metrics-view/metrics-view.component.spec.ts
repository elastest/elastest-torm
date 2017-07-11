import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricsViewComponent } from './metrics-view.component';

describe('MetricsViewComponent', () => {
  let component: MetricsViewComponent;
  let fixture: ComponentFixture<MetricsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
