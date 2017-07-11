import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobFormComponent } from './tjob-form.component';

describe('TjobFormComponent', () => {
  let component: TjobFormComponent;
  let fixture: ComponentFixture<TjobFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
