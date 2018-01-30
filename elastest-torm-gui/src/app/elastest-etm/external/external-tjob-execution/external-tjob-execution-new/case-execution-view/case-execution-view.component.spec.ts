import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CaseExecutionViewComponent } from './case-execution-view.component';

describe('CaseExecutionViewComponent', () => {
  let component: CaseExecutionViewComponent;
  let fixture: ComponentFixture<CaseExecutionViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CaseExecutionViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CaseExecutionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
