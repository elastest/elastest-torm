import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElastestEceComponent } from './elastest-ece.component';

describe('ElastestEceComponent', () => {
  let component: ElastestEceComponent;
  let fixture: ComponentFixture<ElastestEceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElastestEceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElastestEceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
