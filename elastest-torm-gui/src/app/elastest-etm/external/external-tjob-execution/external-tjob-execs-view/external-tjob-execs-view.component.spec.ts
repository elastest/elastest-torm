import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTjobExecsViewComponent } from './external-tjob-execs-view.component';

describe('ExternalTjobExecsViewComponent', () => {
  let component: ExternalTjobExecsViewComponent;
  let fixture: ComponentFixture<ExternalTjobExecsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTjobExecsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTjobExecsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
