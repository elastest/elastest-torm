import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestSpinnerComponent } from './elastest-spinner.component';

describe('ElastestSpinnerComponent', () => {
  let component: ElastestSpinnerComponent;
  let fixture: ComponentFixture<ElastestSpinnerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestSpinnerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
