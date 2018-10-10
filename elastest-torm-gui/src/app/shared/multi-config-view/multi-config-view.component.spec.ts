import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiConfigViewComponent } from './multi-config-view.component';

describe('MultiConfigViewComponent', () => {
  let component: MultiConfigViewComponent;
  let fixture: ComponentFixture<MultiConfigViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MultiConfigViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiConfigViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
