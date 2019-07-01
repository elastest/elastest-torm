import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BrowserCardComponentComponent } from './browser-card-component.component';

describe('BrowserCardComponentComponent', () => {
  let component: BrowserCardComponentComponent;
  let fixture: ComponentFixture<BrowserCardComponentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BrowserCardComponentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BrowserCardComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
