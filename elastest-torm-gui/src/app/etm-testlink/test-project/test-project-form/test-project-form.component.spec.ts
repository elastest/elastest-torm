import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestProjectFormComponent } from './test-project-form.component';

describe('TestProjectFormComponent', () => {
  let component: TestProjectFormComponent;
  let fixture: ComponentFixture<TestProjectFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestProjectFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestProjectFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
