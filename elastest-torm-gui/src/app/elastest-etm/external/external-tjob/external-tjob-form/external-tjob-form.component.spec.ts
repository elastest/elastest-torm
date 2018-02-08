import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTjobFormComponent } from './external-tjob-form.component';

describe('ExternalTjobFormComponent', () => {
  let component: ExternalTjobFormComponent;
  let fixture: ComponentFixture<ExternalTjobFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTjobFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTjobFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
