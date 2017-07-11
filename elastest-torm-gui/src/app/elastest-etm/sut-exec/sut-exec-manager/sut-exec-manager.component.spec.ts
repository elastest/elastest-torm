import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SutExecManagerComponent } from './sut-exec-manager.component';

describe('SutExecManagerComponent', () => {
  let component: SutExecManagerComponent;
  let fixture: ComponentFixture<SutExecManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SutExecManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SutExecManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
