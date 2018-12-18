import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NormalHeightRowComponent } from './normal-height-row.component';

describe('NormalHeightRowComponent', () => {
  let component: NormalHeightRowComponent;
  let fixture: ComponentFixture<NormalHeightRowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NormalHeightRowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NormalHeightRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
