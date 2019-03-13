import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageElasticsearchComponent } from './manage-elasticsearch.component';

describe('ManageElasticsearchComponent', () => {
  let component: ManageElasticsearchComponent;
  let fixture: ComponentFixture<ManageElasticsearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageElasticsearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageElasticsearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
