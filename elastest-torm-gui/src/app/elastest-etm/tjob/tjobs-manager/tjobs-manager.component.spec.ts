import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TjobsManagerComponent } from './tjobs-manager.component';

describe('TjobsManagerComponent', () => {
  let component: TjobsManagerComponent;
  let fixture: ComponentFixture<TjobsManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TjobsManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TjobsManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
