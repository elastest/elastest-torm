import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestLogAnalyzerComponent } from './elastest-log-analyzer.component';

describe('ElastestLogAnalyzerComponent', () => {
  let component: ElastestLogAnalyzerComponent;
  let fixture: ComponentFixture<ElastestLogAnalyzerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestLogAnalyzerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestLogAnalyzerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
