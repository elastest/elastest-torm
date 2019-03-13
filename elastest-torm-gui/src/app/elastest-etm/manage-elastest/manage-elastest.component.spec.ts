import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageElastestComponent } from './manage-elastest.component';

describe('ManageElastestComponent', () => {
  let component: ManageElastestComponent;
  let fixture: ComponentFixture<ManageElastestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageElastestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageElastestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
