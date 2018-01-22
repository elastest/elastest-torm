import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecuteCaseModalComponent } from './execute-case-modal.component';

describe('ExecuteCaseModalComponent', () => {
  let component: ExecuteCaseModalComponent;
  let fixture: ComponentFixture<ExecuteCaseModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecuteCaseModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecuteCaseModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
