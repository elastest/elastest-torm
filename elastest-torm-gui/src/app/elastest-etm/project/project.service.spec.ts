import { TestBed, inject } from '@angular/core/testing';

import { ProjectService } from './project.service';

describe('ProjectServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectService]
    });
  });

  it('should be created', inject([ProjectService], (service: ProjectService) => {
    expect(service).toBeTruthy();
  }));
});
