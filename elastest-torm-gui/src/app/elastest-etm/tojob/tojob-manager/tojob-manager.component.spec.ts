import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TojobManagerComponent } from './tojob-manager.component';

describe('TojobManagerComponent', () => {
  let component: TojobManagerComponent;
  let fixture: ComponentFixture<TojobManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TojobManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TojobManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
