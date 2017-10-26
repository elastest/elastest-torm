import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilesManagerComponent } from './files-manager.component';

describe('FilesManagerComponent', () => {
  let component: FilesManagerComponent;
  let fixture: ComponentFixture<FilesManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilesManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilesManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
