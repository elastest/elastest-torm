import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChildTjobExecsViewComponent } from './child-tjob-execs-view.component';

describe('ChildTjobExecsViewComponent', () => {
  let component: ChildTjobExecsViewComponent;
  let fixture: ComponentFixture<ChildTjobExecsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ChildTjobExecsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChildTjobExecsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
