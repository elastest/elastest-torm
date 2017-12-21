import { EtmChartGroupComponent } from './etm-chart-group.component';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';


describe('EtmChartGroupComponent', () => {
  let component: EtmChartGroupComponent;
  let fixture: ComponentFixture<EtmChartGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EtmChartGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EtmChartGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
