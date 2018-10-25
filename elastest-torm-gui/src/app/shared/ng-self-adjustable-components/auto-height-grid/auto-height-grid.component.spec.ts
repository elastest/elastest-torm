import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AutoHeightGridComponent } from './auto-height-grid.component';

describe('AutoHeightGridComponent', () => {
  let component: AutoHeightGridComponent;
  let fixture: ComponentFixture<AutoHeightGridComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AutoHeightGridComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AutoHeightGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
