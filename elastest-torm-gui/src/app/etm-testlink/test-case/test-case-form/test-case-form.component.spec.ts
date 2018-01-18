import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCaseFormComponent } from './test-case-form.component';

describe('TestCaseFormComponent', () => {
  let component: TestCaseFormComponent;
  let fixture: ComponentFixture<TestCaseFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCaseFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCaseFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
