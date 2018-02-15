import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCasesViewComponent } from './test-cases-view.component';

describe('TestCasesViewComponent', () => {
  let component: TestCasesViewComponent;
  let fixture: ComponentFixture<TestCasesViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCasesViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCasesViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
