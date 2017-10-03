import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestTestEnginesComponent } from './elastest-test-engines.component';

describe('ElastestTestEnginesComponent', () => {
  let component: ElastestTestEnginesComponent;
  let fixture: ComponentFixture<ElastestTestEnginesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestTestEnginesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestTestEnginesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
