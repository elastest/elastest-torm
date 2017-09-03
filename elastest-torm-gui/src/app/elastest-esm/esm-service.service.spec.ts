import { TestBed, inject } from '@angular/core/testing';

import { EsmServiceService } from './esm-service.service';

describe('EsmServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EsmServiceService]
    });
  });

  it('should be created', inject([EsmServiceService], (service: EsmServiceService) => {
    expect(service).toBeTruthy();
  }));
});
