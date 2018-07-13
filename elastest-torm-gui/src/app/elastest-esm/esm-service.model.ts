import { SupportServiceConfigModel } from './support-service.model';

export class EsmServiceModel {
  id: string;
  name: string;
  selected?: boolean;
  manifest: TssManifest;

  constructor(id: string, name: string, selected: boolean, manifest?: TssManifest) {
    this.id = id;
    this.name = name;
    this.selected = selected;
    this.manifest = manifest;
  }

  changeServiceSelection($event): void {
    this.selected = $event.checked;
  }

  public getConfigKeys(): string[] {
    if (this.manifest && this.manifest.config) {
      return this.manifest.getConfigKeys();
    } else {
      return [];
    }
  }
}

export class TssManifest {
  id: string;
  manifestContent: string;
  manifestType: string;
  planId: string;
  serviceId: string;
  endpoints: string;
  config: object;

  constructor() {}

  public initFromJson(json: any) {
    this.id = json.id;
    this.manifestContent = json.manifestContent;
    this.manifestType = json.manifestType;
    this.planId = json.planId;
    this.serviceId = json.serviceId;
    this.endpoints = json.endpoints;

    this.parseConfigListFromJsonString(json.config);
  }

  public parseConfigListFromJsonString(jsonConfigString: any): void {
    this.config = {};
    if (jsonConfigString !== undefined && jsonConfigString !== null && jsonConfigString !== '') {
      let jsonConfig: any;
      try {
        jsonConfig = JSON.parse(jsonConfigString);
      } catch (e) {
        // Already is a json obj
        jsonConfig = jsonConfigString;
      }
      if (jsonConfig !== undefined && jsonConfig !== null) {
        for (let key of Object.keys(jsonConfig)) {
          let singleConfigJson: any = jsonConfig[key];
          let singleConfig: SupportServiceConfigModel = new SupportServiceConfigModel();
          singleConfig.name = key;
          singleConfig.type = singleConfigJson.type;
          singleConfig.label = singleConfigJson.label;
          singleConfig.default = singleConfigJson.default;
          if (singleConfigJson.value !== undefined && singleConfigJson.value !== null) {
            singleConfig.value = singleConfigJson.value;
          } else {
            singleConfig.value = singleConfigJson.default;
          }
          this.config[singleConfig.name] = singleConfig;
        }
      }
    }
  }

  public getConfigKeys(): string[] {
    return Array.from(Object.keys(this.config));
  }
}
