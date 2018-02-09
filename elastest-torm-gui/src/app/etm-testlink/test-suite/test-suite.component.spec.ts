import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TLTestSuiteComponent } from './test-suite.component';

describe('TLTestSuiteComponent', () => {
  let component: TLTestSuiteComponent;
  let fixture: ComponentFixture<TLTestSuiteComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TLTestSuiteComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TLTestSuiteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
