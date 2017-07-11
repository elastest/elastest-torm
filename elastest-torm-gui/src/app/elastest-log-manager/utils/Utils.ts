/*
 * (C) Copyright 2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

export function dateToInputLiteral(date: Date) {
  return date.getUTCFullYear() + '-' +
    pad(date.getUTCMonth() + 1, 2) + '-' +
    pad(date.getUTCDate(), 2) + 'T' +
    pad(date.getUTCHours(), 2) + ':' +
    pad(date.getMinutes(), 2) + ':' +
    pad(date.getSeconds(), 2);
}

export var ES_URL:String = 'http://jenkins:jenkins130@elasticsearch.kurento.org:9200/';
export var INDEX:String = '<kurento-*>';
export var RESULTS_PER_REQUEST:number = 50;

function pad(n:any, width:number, z?:string) {
  z = z || '0';
  n = n + '';
  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

// There is a problem with some packages with client and server because they have the same package name.
var projectForLogger = {
  'org.kurento.test': {
    project: 'kurento-java',
    subProject: 'kurento-integration-tests/kurento-test/src/main/java/'
  },
  'org.kurento.commons': {
    project: 'kurento-java',
    subProject: 'kurento-commons/src/main/java/'
  },
  'org.kurento.client': {
    project: 'kurento-java',
    subProject: 'kurento-client/src/main/java/'
  },
  'org.kurento.jsonrpc.client': {
    project: 'kurento-java',
    subProject: 'kurento-jsonrpc/kurento-jsonrpc-client/src/main/java/'
  },
  'org.kurento.jsonrpc.message': {
    project: 'kurento-java',
    subProject: 'kurento-jsonrpc/kurento-jsonrpc-client/src/main/java/'
  },
  'org.kurento.jsonrpc.internal.client': {
    project: 'kurento-java',
    subProject: 'kurento-jsonrpc/kurento-jsonrpc-client/src/main/java/'
  },
  'org.kurento.jsonrpc.internal.http': {
    project: 'kurento-java',
    subProject: 'kurento-jsonrpc/kurento-jsonrpc-server/src/main/java/'
  },
  'org.kurento.jsonrpc.internal.server': {
    project: 'kurento-java',
    subProject: 'kurento-jsonrpc/kurento-jsonrpc-server/src/main/java/'
  },
  'org.kurento.repository': {
    project: 'kurento-java',
    subProject: 'kurento-repository/kurento-repository-client/src/main/java/'
  },
  'org.kurento.kms.controller': {
    project: 'kurento-cluster',
    subProject: 'kms-controller/src/main/java/'
  },
  'org.kurento.kmscluster.controller': {
    project: 'kurento-cluster',
    subProject: 'kmscluster-controller/src/main/java/'
  },
  'org.kurento.hazelcast': {
    project: 'kurento-cluster',
    subProject: 'kmscluster-controller/src/main/java/'
  }
};

export function getGerritUrl(logger:string, line:number) {
  for (var key in projectForLogger) {
    let value = projectForLogger[key];
    if (logger.indexOf(key) !== -1) {
      return 'https://code.kurento.org/gitweb?p=' + value.project + '.git;a=blob;f=' + value.subProject +
        logger.replace(/\./g, '/') + '.java;hb=HEAD#l' + line;
    }
  }
  return '';
}
