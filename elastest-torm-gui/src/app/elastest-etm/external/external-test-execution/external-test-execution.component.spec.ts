import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTestExecutionComponent } from './external-test-execution.component';

describe('ExternalTestExecutionComponent', () => {
  let component: ExternalTestExecutionComponent;
  let fixture: ComponentFixture<ExternalTestExecutionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTestExecutionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTestExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
