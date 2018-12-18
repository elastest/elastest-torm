import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CenteredElementComponent } from './centered-element.component';

describe('CenteredElementComponent', () => {
  let component: CenteredElementComponent;
  let fixture: ComponentFixture<CenteredElementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CenteredElementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CenteredElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
