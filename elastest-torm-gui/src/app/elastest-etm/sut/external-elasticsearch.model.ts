export class ExternalElasticsearch {
  id: number;
  ip: string;
  port: string;
  path: string;
  user: string;
  pass: string;
  indices: string;

  constructor(externalElasticsearchJson: any = undefined) {
    if (!externalElasticsearchJson) {
      this.id = 0;
      this.ip = '';
      this.port = '';
      this.path = '';
      this.user = '';
      this.pass = '';
      this.indices = '';
    } else {
      this.id = externalElasticsearchJson.id;
      this.ip = externalElasticsearchJson.ip;
      this.port = externalElasticsearchJson.port;
      this.path = externalElasticsearchJson.path;
      this.user = externalElasticsearchJson.user;
      this.pass = externalElasticsearchJson.pass;
      this.indices = externalElasticsearchJson.indices;
    }
  }
}
