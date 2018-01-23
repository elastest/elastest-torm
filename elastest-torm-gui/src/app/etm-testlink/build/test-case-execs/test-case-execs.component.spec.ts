import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCaseExecsComponent } from './test-case-execs.component';

describe('TestCaseExecsComponent', () => {
  let component: TestCaseExecsComponent;
  let fixture: ComponentFixture<TestCaseExecsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCaseExecsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCaseExecsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
