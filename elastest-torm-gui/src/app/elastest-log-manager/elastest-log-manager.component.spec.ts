import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestLogManagerComponent } from './elastest-log-manager.component';

describe('ElastestLogManagerComponent', () => {
  let component: ElastestLogManagerComponent;
  let fixture: ComponentFixture<ElastestLogManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestLogManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestLogManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
