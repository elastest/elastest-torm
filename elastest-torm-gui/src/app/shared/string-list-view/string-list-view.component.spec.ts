import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StringListViewComponent } from './string-list-view.component';

describe('StringListViewComponent', () => {
  let component: StringListViewComponent;
  let fixture: ComponentFixture<StringListViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StringListViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StringListViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
