import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCaseComponent } from './test-case.component';

describe('TestCaseComponent', () => {
  let component: TestCaseComponent;
  let fixture: ComponentFixture<TestCaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
