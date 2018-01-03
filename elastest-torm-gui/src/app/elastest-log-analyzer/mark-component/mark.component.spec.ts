import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkComponent } from './mark.component';

describe('MarkComponent', () => {
  let component: MarkComponent;
  let fixture: ComponentFixture<MarkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MarkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MarkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
