import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SelfAdjustableCardComponent } from './self-adjustable-card.component';

describe('SelfAdjustableCardComponent', () => {
  let component: SelfAdjustableCardComponent;
  let fixture: ComponentFixture<SelfAdjustableCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelfAdjustableCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelfAdjustableCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
