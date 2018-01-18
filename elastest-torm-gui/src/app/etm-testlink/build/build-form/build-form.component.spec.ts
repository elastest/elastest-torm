import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BuildFormComponent } from './build-form.component';

describe('BuildFormComponent', () => {
  let component: BuildFormComponent;
  let fixture: ComponentFixture<BuildFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BuildFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BuildFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
