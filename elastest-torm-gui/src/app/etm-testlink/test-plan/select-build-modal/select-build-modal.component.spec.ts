import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectBuildModalComponent } from './select-build-modal.component';

describe('SelectBuildModalComponent', () => {
  let component: SelectBuildModalComponent;
  let fixture: ComponentFixture<SelectBuildModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectBuildModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectBuildModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
