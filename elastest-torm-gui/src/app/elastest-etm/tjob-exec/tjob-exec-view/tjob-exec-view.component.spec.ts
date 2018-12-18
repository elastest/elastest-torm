import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobExecViewComponent } from './tjob-exec-view.component';

describe('TjobExecViewComponent', () => {
  let component: TjobExecViewComponent;
  let fixture: ComponentFixture<TjobExecViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobExecViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobExecViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
