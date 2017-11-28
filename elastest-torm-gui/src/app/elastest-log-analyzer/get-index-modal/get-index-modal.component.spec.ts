import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GetIndexModalComponent } from './get-index-modal.component';

describe('GetIndexModalComponent', () => {
  let component: GetIndexModalComponent;
  let fixture: ComponentFixture<GetIndexModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GetIndexModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GetIndexModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
