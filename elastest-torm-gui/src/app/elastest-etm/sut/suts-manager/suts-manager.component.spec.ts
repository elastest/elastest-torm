import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SutsManagerComponent } from './suts-manager.component';

describe('SutsManagerComponent', () => {
  let component: SutsManagerComponent;
  let fixture: ComponentFixture<SutsManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SutsManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SutsManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
