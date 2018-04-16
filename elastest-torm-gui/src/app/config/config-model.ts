export interface ConfigModel {
  hostName: string;
  host: string;
  proxyHost: string;
  hostApi: string;
  hostElasticsearch: string;
  hostEIM: string;
  hostWsServer: string;
  eusHost: string;
  eusPort: string;
  eusServiceUrlNoPath: string;
  eusServiceUrl: string;
  eusWebSocketUrl: string;
  elasTestExecMode: string;
  testLinkStarted: boolean;
  httpsSecure: boolean;
  empGrafanaUrl: string;
  edmCommandUrl: string;
}
