import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceGuiComponent } from './service-gui.component';

describe('ServiceGuiComponent', () => {
  let component: ServiceGuiComponent;
  let fixture: ComponentFixture<ServiceGuiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceGuiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceGuiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
