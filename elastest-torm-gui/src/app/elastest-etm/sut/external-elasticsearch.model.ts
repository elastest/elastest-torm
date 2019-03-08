import { MultiConfigModel } from '../../shared/multi-config-view/multi-config-view.component';

export class ExternalElasticsearch {
  id: number;
  protocol: 'http' | 'https' | ''; // On add new, add too in getProtocolsList
  ip: string;
  port: string;
  path: string;
  user: string;
  pass: string;
  indices: string;
  streamFields: string;
  fieldFilters: MultiConfigModel[];

  constructor(externalElasticsearchJson: any = undefined) {
    if (!externalElasticsearchJson) {
      this.id = 0;
      this.protocol = 'http';
      this.ip = '';
      this.port = '';
      this.path = '';
      this.user = '';
      this.pass = '';
      this.indices = '';
      this.streamFields = '';
      this.fieldFilters = [];
    } else {
      this.id = externalElasticsearchJson.id;
      this.protocol = externalElasticsearchJson.protocol;
      this.ip = externalElasticsearchJson.ip;
      this.port = externalElasticsearchJson.port;
      this.path = externalElasticsearchJson.path;
      this.user = externalElasticsearchJson.user;
      this.pass = externalElasticsearchJson.pass;
      this.indices = externalElasticsearchJson.indices;
      this.streamFields = externalElasticsearchJson.streamFields ? externalElasticsearchJson.streamFields : '';
      this.fieldFilters = externalElasticsearchJson.fieldFilters ? externalElasticsearchJson.fieldFilters : [];
    }
  }

  public getProtocolsList(): string[] {
    return ['http', 'https'];
  }
}
