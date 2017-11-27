import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RefeshComponent } from './refesh.component';

describe('RefeshComponent', () => {
  let component: RefeshComponent;
  let fixture: ComponentFixture<RefeshComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RefeshComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RefeshComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
