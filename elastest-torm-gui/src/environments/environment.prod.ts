export const environment: { production: boolean, hostElasticSearch: string, hostEIM: string, eus: string } = {
  production: true,
  hostElasticSearch: 'localhost:9200',
  hostEIM: 'localhost:37004',
  eus: window.location.hostname + ':8040'
};
