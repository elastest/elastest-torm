/**
 * Created by frdiaz on 10/02/2017.
 */

import { ConfigurationService } from "./configuration-service.service";

export function configServiceFactory(configurationService: ConfigurationService) {
  return () => configurationService.load();

}
