import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AutoHeightRowComponent } from './auto-height-row.component';

describe('AutoHeightRowComponent', () => {
  let component: AutoHeightRowComponent;
  let fixture: ComponentFixture<AutoHeightRowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AutoHeightRowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AutoHeightRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
