import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalTjobExecutionNewComponent } from './external-tjob-execution-new.component';

describe('ExternalTjobExecutionNewComponent', () => {
  let component: ExternalTjobExecutionNewComponent;
  let fixture: ComponentFixture<ExternalTjobExecutionNewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalTjobExecutionNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalTjobExecutionNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
