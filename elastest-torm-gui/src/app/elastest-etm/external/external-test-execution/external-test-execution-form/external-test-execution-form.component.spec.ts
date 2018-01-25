import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTestExecutionFormComponent } from './external-test-execution-form.component';

describe('ExternalTestExecutionFormComponent', () => {
  let component: ExternalTestExecutionFormComponent;
  let fixture: ComponentFixture<ExternalTestExecutionFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTestExecutionFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTestExecutionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
