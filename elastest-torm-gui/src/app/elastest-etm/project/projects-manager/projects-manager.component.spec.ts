import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectsManagerComponent } from './projects-manager.component';

describe('ProjectsManagerComponent', () => {
  let component: ProjectsManagerComponent;
  let fixture: ComponentFixture<ProjectsManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectsManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectsManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
