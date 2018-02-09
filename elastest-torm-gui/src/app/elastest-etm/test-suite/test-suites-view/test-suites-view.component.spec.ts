import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestSuitesViewComponent } from './test-suites-view.component';

describe('TestSuitesViewComponent', () => {
  let component: TestSuitesViewComponent;
  let fixture: ComponentFixture<TestSuitesViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestSuitesViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestSuitesViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
