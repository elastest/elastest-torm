import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobManagerComponent } from './tjob-manager.component';

describe('TjobManagerComponent', () => {
  let component: TjobManagerComponent;
  let fixture: ComponentFixture<TjobManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
