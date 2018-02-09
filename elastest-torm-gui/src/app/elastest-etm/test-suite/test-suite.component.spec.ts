import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestSuiteComponent } from './test-suite.component';

describe('TestSuiteComponent', () => {
  let component: TestSuiteComponent;
  let fixture: ComponentFixture<TestSuiteComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestSuiteComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestSuiteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
