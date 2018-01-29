import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTjobExecutionComponent } from './external-tjob-execution.component';

describe('ExternalTjobExecutionComponent', () => {
  let component: ExternalTjobExecutionComponent;
  let fixture: ComponentFixture<ExternalTjobExecutionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTjobExecutionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTjobExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
