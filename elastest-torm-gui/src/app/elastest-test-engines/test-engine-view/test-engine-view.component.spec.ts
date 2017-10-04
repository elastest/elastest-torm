import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestEngineViewComponent } from './test-engine-view.component';

describe('TestEngineViewComponent', () => {
  let component: TestEngineViewComponent;
  let fixture: ComponentFixture<TestEngineViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestEngineViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestEngineViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
