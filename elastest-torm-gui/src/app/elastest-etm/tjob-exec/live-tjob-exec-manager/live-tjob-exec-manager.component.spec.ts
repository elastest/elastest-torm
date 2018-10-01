import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LiveTjobExecManagerComponent } from './live-tjob-exec-manager.component';

describe('LiveTjobExecManagerComponent', () => {
  let component: LiveTjobExecManagerComponent;
  let fixture: ComponentFixture<LiveTjobExecManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LiveTjobExecManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LiveTjobExecManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
