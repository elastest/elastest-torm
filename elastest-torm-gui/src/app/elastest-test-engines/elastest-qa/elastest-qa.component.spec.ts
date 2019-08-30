import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestQaComponent } from './elastest-qa.component';

describe('ElastestQaComponent', () => {
  let component: ElastestQaComponent;
  let fixture: ComponentFixture<ElastestQaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestQaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestQaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
