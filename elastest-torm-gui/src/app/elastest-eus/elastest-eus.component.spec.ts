import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestEusComponent } from './elastest-eus.component';

describe('ElastestEusComponent', () => {
  let component: ElastestEusComponent;
  let fixture: ComponentFixture<ElastestEusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestEusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestEusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
