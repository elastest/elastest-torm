import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ParentTjobExecReportViewComponent } from './parent-tjob-exec-report-view.component';

describe('ParentTjobExecReportViewComponent', () => {
  let component: ParentTjobExecReportViewComponent;
  let fixture: ComponentFixture<ParentTjobExecReportViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParentTjobExecReportViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParentTjobExecReportViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
