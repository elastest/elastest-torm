import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPlanFormComponent } from './test-plan-form.component';

describe('TestPlanFormComponent', () => {
  let component: TestPlanFormComponent;
  let fixture: ComponentFixture<TestPlanFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestPlanFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestPlanFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
