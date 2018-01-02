import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowMessageModalComponent } from './show-message-modal.component';

describe('ShowMessageModalComponent', () => {
  let component: ShowMessageModalComponent;
  let fixture: ComponentFixture<ShowMessageModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShowMessageModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowMessageModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
