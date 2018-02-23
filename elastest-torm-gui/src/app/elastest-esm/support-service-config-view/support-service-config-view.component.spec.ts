import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SupportServiceConfigViewComponent } from './support-service-config-view.component';

describe('SupportServiceConfigViewComponent', () => {
  let component: SupportServiceConfigViewComponent;
  let fixture: ComponentFixture<SupportServiceConfigViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SupportServiceConfigViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SupportServiceConfigViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
