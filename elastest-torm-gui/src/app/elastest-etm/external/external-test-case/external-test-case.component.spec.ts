import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTestCaseComponent } from './external-test-case.component';

describe('ExternalTestCaseComponent', () => {
  let component: ExternalTestCaseComponent;
  let fixture: ComponentFixture<ExternalTestCaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTestCaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTestCaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
