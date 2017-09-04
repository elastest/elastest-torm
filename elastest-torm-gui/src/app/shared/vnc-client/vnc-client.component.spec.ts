import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VncClientComponent } from './vnc-client.component';

describe('VncClientComponent', () => {
  let component: VncClientComponent;
  let fixture: ComponentFixture<VncClientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VncClientComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VncClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
