import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EdmContainerComponent } from './edm-container.component';

describe('EdmContainerComponent', () => {
  let component: EdmContainerComponent;
  let fixture: ComponentFixture<EdmContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EdmContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EdmContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
