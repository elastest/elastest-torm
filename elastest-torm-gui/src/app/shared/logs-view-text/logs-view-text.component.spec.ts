import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LogsViewTextComponent } from './logs-view-text.component';

describe('LogsViewTextComponent', () => {
  let component: LogsViewTextComponent;
  let fixture: ComponentFixture<LogsViewTextComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LogsViewTextComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LogsViewTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
