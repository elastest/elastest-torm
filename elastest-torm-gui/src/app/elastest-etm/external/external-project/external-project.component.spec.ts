import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalProjectComponent } from './external-project.component';

describe('ExternalProjectComponent', () => {
  let component: ExternalProjectComponent;
  let fixture: ComponentFixture<ExternalProjectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalProjectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
