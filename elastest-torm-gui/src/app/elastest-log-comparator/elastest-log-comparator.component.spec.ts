import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestLogComparatorComponent } from './elastest-log-comparator.component';

describe('ElastestLogComparatorComponent', () => {
  let component: ElastestLogComparatorComponent;
  let fixture: ComponentFixture<ElastestLogComparatorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestLogComparatorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestLogComparatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
