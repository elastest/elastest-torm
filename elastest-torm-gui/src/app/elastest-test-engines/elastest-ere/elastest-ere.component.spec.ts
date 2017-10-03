import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestEreComponent } from './elastest-ere.component';

describe('ElastestEreComponent', () => {
  let component: ElastestEreComponent;
  let fixture: ComponentFixture<ElastestEreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestEreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestEreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
