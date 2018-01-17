import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmTestlinkComponent } from './etm-testlink.component';

describe('EtmTestlinkComponent', () => {
  let component: EtmTestlinkComponent;
  let fixture: ComponentFixture<EtmTestlinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmTestlinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmTestlinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
