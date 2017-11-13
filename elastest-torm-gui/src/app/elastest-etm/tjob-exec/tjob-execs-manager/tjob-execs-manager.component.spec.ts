import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TJobExecsManagerComponent } from './tjob-execs-manager.component';

describe('TJobExecsManagerComponent', () => {
  let component: TJobExecsManagerComponent;
  let fixture: ComponentFixture<TJobExecsManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TJobExecsManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TJobExecsManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
