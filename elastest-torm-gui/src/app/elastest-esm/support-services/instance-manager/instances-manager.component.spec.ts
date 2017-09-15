import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServicesManagerComponent } from './services-manager.component';

describe('ServicesManagerComponent', () => {
  let component: ServicesManagerComponent;
  let fixture: ComponentFixture<ServicesManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServicesManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServicesManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
