import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EmpContainerComponent } from './emp-container.component';

describe('EmpContainerComponent', () => {
  let component: EmpContainerComponent;
  let fixture: ComponentFixture<EmpContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EmpContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmpContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
