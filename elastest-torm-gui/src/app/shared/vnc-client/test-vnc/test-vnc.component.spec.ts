import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestVncComponent } from './test-vnc.component';

describe('TestVncComponent', () => {
  let component: TestVncComponent;
  let fixture: ComponentFixture<TestVncComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestVncComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestVncComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
