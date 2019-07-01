import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CrossbrowserComponentComponent } from './crossbrowser-component.component';

describe('CrossbrowserComponentComponent', () => {
  let component: CrossbrowserComponentComponent;
  let fixture: ComponentFixture<CrossbrowserComponentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CrossbrowserComponentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CrossbrowserComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
