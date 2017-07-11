import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadPreviousViewComponent } from './load-previous-view.component';

describe('LoadPreviousViewComponent', () => {
  let component: LoadPreviousViewComponent;
  let fixture: ComponentFixture<LoadPreviousViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoadPreviousViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadPreviousViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
