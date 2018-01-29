import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTjobComponent } from './external-tjob.component';

describe('ExternalTjobComponent', () => {
  let component: ExternalTjobComponent;
  let fixture: ComponentFixture<ExternalTjobComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTjobComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTjobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
