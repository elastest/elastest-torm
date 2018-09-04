import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EtmJenkinsComponent } from './etm-jenkins.component';

describe('EtmJenkinsComponent', () => {
  let component: EtmJenkinsComponent;
  let fixture: ComponentFixture<EtmJenkinsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmJenkinsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmJenkinsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
