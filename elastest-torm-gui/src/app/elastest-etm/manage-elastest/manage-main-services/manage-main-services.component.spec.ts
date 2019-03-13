import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageMainServicesComponent } from './manage-main-services.component';

describe('ManageMainServicesComponent', () => {
  let component: ManageMainServicesComponent;
  let fixture: ComponentFixture<ManageMainServicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageMainServicesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageMainServicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
