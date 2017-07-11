import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobExecManagerComponent } from './tjob-exec-manager.component';

describe('TjobExecManagerComponent', () => {
  let component: TjobExecManagerComponent;
  let fixture: ComponentFixture<TjobExecManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobExecManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobExecManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
