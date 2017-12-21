import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmLogsGroupComponent } from './etm-logs-group.component';

describe('EtmLogsGroupComponent', () => {
  let component: EtmLogsGroupComponent;
  let fixture: ComponentFixture<EtmLogsGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmLogsGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmLogsGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
