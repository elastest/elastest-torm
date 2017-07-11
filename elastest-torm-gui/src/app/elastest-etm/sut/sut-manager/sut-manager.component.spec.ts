import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SutManagerComponent } from './sut-manager.component';

describe('SutManagerComponent', () => {
  let component: SutManagerComponent;
  let fixture: ComponentFixture<SutManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SutManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SutManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
