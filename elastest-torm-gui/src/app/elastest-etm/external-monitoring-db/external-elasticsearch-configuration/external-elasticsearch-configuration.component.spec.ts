import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalElasticsearchConfigurationComponent } from './external-elasticsearch-configuration.component';

describe('ExternalElasticsearchConfigurationComponent', () => {
  let component: ExternalElasticsearchConfigurationComponent;
  let fixture: ComponentFixture<ExternalElasticsearchConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExternalElasticsearchConfigurationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalElasticsearchConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
