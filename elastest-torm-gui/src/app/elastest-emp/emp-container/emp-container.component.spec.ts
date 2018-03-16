import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpmContainerComponent } from './epm-container.component';

describe('EpmContainerComponent', () => {
  let component: EpmContainerComponent;
  let fixture: ComponentFixture<EpmContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpmContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpmContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
