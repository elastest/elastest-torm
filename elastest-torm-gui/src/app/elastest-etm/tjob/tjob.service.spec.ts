import { TestBed, inject } from '@angular/core/testing';

import { TjobService } from './tjob.service';

describe('TjobService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TjobService]
    });
  });

  it('should be created', inject([TjobService], (service: TjobService) => {
    expect(service).toBeTruthy();
  }));
});
