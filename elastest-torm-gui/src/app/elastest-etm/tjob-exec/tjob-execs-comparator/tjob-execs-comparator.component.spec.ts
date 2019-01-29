import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobExecsComparatorComponent } from './tjob-execs-comparator.component';

describe('TjobExecsComparatorComponent', () => {
  let component: TjobExecsComparatorComponent;
  let fixture: ComponentFixture<TjobExecsComparatorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobExecsComparatorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobExecsComparatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
