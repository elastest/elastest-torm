import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPlanComponent } from './test-plan.component';

describe('TestPlanComponent', () => {
  let component: TestPlanComponent;
  let fixture: ComponentFixture<TestPlanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestPlanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
