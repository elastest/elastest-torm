import { Subscription, Observable } from 'rxjs';

export class PullingObjectModel {
  subscription: Subscription;
  observable: Observable<any>;

  constructor() {}
}
