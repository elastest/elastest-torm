import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTestExecutionsViewComponent } from './external-test-executions-view.component';

describe('ExternalTestExecutionsViewComponent', () => {
  let component: ExternalTestExecutionsViewComponent;
  let fixture: ComponentFixture<ExternalTestExecutionsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTestExecutionsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTestExecutionsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
