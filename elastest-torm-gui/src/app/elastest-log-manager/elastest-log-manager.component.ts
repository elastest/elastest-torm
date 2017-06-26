import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';

import { dateToInputLiteral } from './utils/Utils';
import { ElasticSearchService } from './services/elasticSearch.service';
// import { GridComponent } from './grid/components/grid.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-elastest-log-manager',
  templateUrl: './elastest-log-manager.component.html',
  styleUrls: ['./elastest-log-manager.component.scss'],
})
export class ElastestLogManagerComponent implements OnInit {

  public _scroll_id: string;
  public noMore: boolean = false;
  public dataForAdding;
  public onlyTable: boolean = false;
  public goToLogManager: string;

  public indices = [];
  public clusterSelected: string;
  public defaultFrom = new Date(new Date().valueOf() - (10 * 60 * 60 * 1000));
  public defaultTo = new Date(new Date().valueOf() - (60 * 60 * 1000));

  // show/hide the grid and spinner
  public rowData: any[] = [];
  public showGrid: boolean;
  public showError: boolean;
  public waiting: boolean = false;

  public errorMsg: string = '';

  public sutlogsType: boolean = false;
  public testlogsType: boolean = true;

  public debugLevel: boolean = false;
  public infoLevel: boolean = false;
  public warnLevel: boolean = false;
  public errorLevel: boolean = false;

  public useTail: boolean = false;

  public urlElastic: string = 'http://localhost:9200/';
  public clusterName: string;
  public indexName: string;
  public hosts: string;
  public message: string;
  public componentType: string;
  public maxResults: number = 500;
  public urlCopied: string;
  public showLoadMore: boolean = false;
  public showPauseTail: boolean = false;
  public showClearData: boolean = false;
  public tailInterval: number;

  constructor(public _elasticSearchService: ElasticSearchService, public activatedRoute: ActivatedRoute) {
    let params: any = this.activatedRoute.snapshot.params;
    this.showGrid = false;
    this.showError = false;
    let autoSearch: boolean = false;

    this.goToLogManager = location.search.replace('onlyTable=true', 'onlyTable=false');

    if (params.onlyTable === 'false') {
      autoSearch = true;
      this.onlyTable = false;
    } else if (params.onlyTable === 'true') {
      autoSearch = true;
      this.onlyTable = true;
    }

    if (params.urlElastic !== undefined && params.urlElastic !== null) {
      this.urlElastic = decodeURIComponent(params.urlElastic);
      autoSearch = true;
    }

    if (params.clusterName !== undefined && params.clusterName !== null) {
      this.clusterName = decodeURIComponent(params.clusterName);
      autoSearch = true;
    }

    if (params.indexName !== undefined && params.indexName !== null) {
      this.indexName = decodeURIComponent(params.indexName);
      autoSearch = true;
    }

    if (params.message !== undefined && params.message !== null) {
      this.message = decodeURIComponent(params.message);
      autoSearch = true;
    }

    if (params.hosts !== undefined && params.hosts !== null) {
      this.hosts = decodeURIComponent(params.hosts);
      autoSearch = true;
    }

    if (params.componentType !== undefined && params.componentType !== null) {
      this.componentType = decodeURIComponent(params.componentType);
      autoSearch = true;
    }

    if (params.maxResults !== undefined && params.maxResults !== null) {
      this.maxResults = parseInt(decodeURIComponent(params.maxResults));
      autoSearch = true;
    }

    // if (params.sutlogsType !== undefined && params.sutlogsType !== null && params.sutlogsType === 'false') {
    //   autoSearch = true;
    //   this.sutlogsType = false;
    // } else if (params.sutlogsType === 'true') {
    //   autoSearch = true;
    //   this.sutlogsType = true;
    // }

    if (params.testlogsType !== undefined && params.testlogsType !== null && params.testlogsType === 'false') {
      autoSearch = true;
      this.testlogsType = false;
    } else if (params.testlogsType === 'true') {
      autoSearch = true;
      this.testlogsType = true;
    }


    if (params.debugLevel !== undefined && params.debugLevel !== null && params.debugLevel === 'false') {
      autoSearch = true;
      this.debugLevel = false;
    } else if (params.debugLevel === 'true') {
      autoSearch = true;
      this.debugLevel = true;
    }

    if (params.infoLevel !== undefined && params.infoLevel !== null && params.infoLevel === 'false') {
      autoSearch = true;
      this.infoLevel = false;
    } else if (params.infoLevel === 'true') {
      autoSearch = true;
      this.infoLevel = true;
    }

    if (params.warnLevel !== undefined && params.warnLevel !== null && params.warnLevel === 'false') {
      autoSearch = true;
      this.warnLevel = false;
    } else if (params.warnLevel === 'true') {
      autoSearch = true;
      this.warnLevel = true;
    }

    if (params.errorLevel !== undefined && params.errorLevel !== null && params.errorLevel === 'false') {
      autoSearch = true;
      this.errorLevel = false;
    } else if (params.errorLevel === 'true') {
      autoSearch = true;
      this.errorLevel = true;
    }

    if (params.from !== undefined && params.from !== null) {
      autoSearch = true;
      let dates = decodeURIComponent(params.from.split('T'));
      let fromDate = dates[0].split('-').map(Number);
      let fromHour = dates[1].split(':').map(Number);
      this.defaultFrom = new Date(Date.UTC(fromDate[0], (fromDate[1] - 1), fromDate[2], fromHour[0], fromHour[1],
        fromHour[2]));
    }

    if (params.to !== undefined && params.to !== null) {
      autoSearch = true;
      let dates = decodeURIComponent(params.to.split('T'));
      let fromDate = dates[0].split('-').map(Number);
      let fromHour = dates[1].split(':').map(Number);
      this.defaultTo = new Date(Date.UTC(fromDate[0], (fromDate[1] - 1), fromDate[2], fromHour[0], fromHour[1],
        fromHour[2]));
    }

    if (autoSearch) {
      this.search(dateToInputLiteral(this.defaultFrom), dateToInputLiteral(this.defaultTo));
    }

    let url = this.urlElastic + '_mapping';
    this.updateIndices(url);
  }

  ngOnInit() {
  }

  public updateIndices(url: string) {
    this.indices = [];
    this._elasticSearchService.getIndices(url).subscribe(
      data => {
        Object.keys(data).sort().map(e => {

          if (e.split('-').length === 3) {
            let cluster: string = e.split('-')[1];
            let date: string = e.split('-')[2];
            let elementExist = this.indices.filter(function (e) {
              return e.cluster.name === cluster;
            });

            if (elementExist.length === 0) {

              let element = {
                'cluster': {
                  'name': cluster,
                  'dates': {
                    'init': date,
                    'end': date
                  }
                }
              };
              this.indices.push(element);
            } else {
              elementExist[0].cluster.dates.end = date;
            }
          }
        }
        );
        let elementEmpty = {
          'cluster': {
            'name': '----',
            'dates': {
              'init': '',
              'end': ''
            }
          }
        };
        this.indices.push(elementEmpty);
        this.indices.sort();
      }
    );
  }

  public processCommaSeparatedValue(value: string) {
    if (value === undefined) {
      return [];
    }
    let array: string[] = value.split(',').map(s => s.trim());
    if (array.length === 1 && array[0] === '') {
      array = [];
    }
    return array;
  }

  public addTermFilter(queryes: any, field: string, values: string[]) {

    let filter: any = {};
    if (values.length > 1) {
      filter[field] = values;
      queryes.indices.query.bool.filter.bool.must.push({
        'terms': filter
      });
    } else if (values.length === 1) {
      if (field === 'message') {
        let filterValue = '{\"multi_match\": {\"query\" : \"' + values.join(' ') +
          '\",\"type\": \"phrase\", \"fields\": [\"message\", \"logmessage\"] }}';
        queryes.indices.query.bool.filter.bool.must.push(JSON.parse(filterValue));
      } else {
        filter[field] = values[0];
        let filterValue = '{\"match\":{\"' + field + '\" : {\"query\" : \"' + values.join(' ') +
          '\",\"type\": \"phrase\" }}}';
        queryes.indices.query.bool.filter.bool.must.push(JSON.parse(filterValue));
      }
    }
  }

  public copyToClipboard() {
    var copyTextarea = document.querySelector('.js-copytextarea')[0];
    copyTextarea.select();
    document.execCommand('copy');
  }

  public generateCopyUrl(from: string, to: string) {

    this.urlCopied = document.URL + '?';
    if (this.urlElastic !== undefined) {
      this.urlCopied += 'urlElastic=' + encodeURIComponent(this.urlElastic) + '&';
    }

    if (this.clusterName !== undefined) {
      this.urlCopied += 'clusterName=' + encodeURIComponent(this.clusterName) + '&';
    }

    if (this.indexName !== undefined) {
      this.urlCopied += 'indexName=' + encodeURIComponent(this.indexName) + '&';
    }

    if (this.message !== undefined) {
      this.urlCopied += 'message=' + encodeURIComponent(this.message) + '&';
    }

    if (this.hosts !== undefined) {
      this.urlCopied += 'hosts=' + encodeURIComponent(this.hosts) + '&';
    }

    if (this.componentType !== undefined) {
      this.urlCopied += 'componentType=' + encodeURIComponent(this.componentType) + '&';
    }

    if (this.maxResults !== undefined) {
      this.urlCopied += 'maxResults=' + encodeURIComponent(String(this.maxResults)) + '&';
    }

    if (this.sutlogsType !== undefined) {
      this.urlCopied += 'sutlogsType=' + encodeURIComponent(String(this.sutlogsType)) + '&';
    }

    if (this.testlogsType !== undefined) {
      this.urlCopied += 'testlogsType=' + encodeURIComponent(String(this.testlogsType)) + '&';
    }

    if (this.debugLevel !== undefined) {
      this.urlCopied += 'debugLevel=' + encodeURIComponent(String(this.debugLevel)) + '&';
    }

    if (this.infoLevel !== undefined) {
      this.urlCopied += 'infoLevel=' + encodeURIComponent(String(this.infoLevel)) + '&';
    }

    if (this.warnLevel !== undefined) {
      this.urlCopied += 'warnLevel=' + encodeURIComponent(String(this.warnLevel)) + '&';
    }

    if (this.errorLevel !== undefined) {
      this.urlCopied += 'errorLevel=' + encodeURIComponent(String(this.errorLevel)) + '&';
    }

    if (from !== undefined) {
      this.urlCopied += 'from=' + encodeURIComponent(from) + '&';
    }

    if (to !== null) {
      this.urlCopied += 'to=' + encodeURIComponent(to) + '&';
    }
  }

  // Used in html file
  public updateClusterSelected(event: Event): void {
    const value: string = (<HTMLSelectElement>event.srcElement).value;
    if (value !== '----') {
      let cluster = this.indices.filter(function (e) {
        return e.cluster.name === value;
      });

      this.defaultFrom = new Date(Date.UTC(cluster[0].cluster.dates.init.split('.')[0],
        (cluster[0].cluster.dates.init.split('.')[1] - 1), cluster[0].cluster.dates.init.split('.')[2], 0, 0, 0));
      this.defaultTo = new Date(Date.parse(cluster[0].cluster.dates.end) + (23 * 60 * 60 * 1000) +
        (59 * 60 * 1000) + 59 * 1000);

      this.clusterSelected = value;
      this.clusterName = value;
    } else {
      this.clusterName = '';
    }
  }

  // Used in html file
  public updateUrlElastic(event: Event): void {
    var value: string;
    if (event === undefined) {
      value = this.urlElastic;
    } else {
      value = (<HTMLSelectElement>event.srcElement).value;
    }
    this.clusterName = '';
    this.updateIndices(value + '_mapping');
  }

  // Used in html file
  public getDefaultFromValue() {
    return dateToInputLiteral(this.defaultFrom);
  }

  // Used in html file
  public getDefaultToValue() {
    return dateToInputLiteral(this.defaultTo);
  }

  public getDifferenceDates(from: string, to: string): number {
    let date1: string[] = to.split('T')[0].split('-');
    let date2: string[] = from.split('T')[0].split('-');

    let date1_: Date = new Date(to);
    let date2_: Date = new Date(from);

    var date1Unixtime: number = (date1_.getTime() / 1000);
    var date2Unixtime: number = (date2_.getTime() / 1000);

    var timeDifference = date2Unixtime - date1Unixtime;

    return Math.abs(timeDifference / 60 / 60 / 24);
  }

  public tailSearch(tail: boolean) {
    this.useTail = tail;
    if (tail) {
      this.tailInterval = setInterval(() => {
        // In this case, to will be 'now'
        this.search(dateToInputLiteral(this.defaultFrom), undefined, true);
      }, 1000);
    } else {
      clearInterval(this.tailInterval);
      this.tailInterval = undefined;
    }
  }

  // Used in html file
  public setUseTail(tail: boolean) {
    this.useTail = tail;
  }

  // Used in html file
  public updateDatesForMoreDate(event) {
    this.dataForAdding = event;
  }

  // Used in html file
  public updateRows(event) {
    this.rowData = event;
  }

  public clearData() {
    this.rowData = [];
    clearInterval(this.tailInterval);
    this.tailInterval = undefined;
    this.showGrid = false;
    this.showLoadMore = false;
    this.showPauseTail = false;
    this.showClearData = false;
    this._scroll_id = '';
  }

  // Used in html file
  public addMore() {
    let position = this.dataForAdding.position;
    let from = this.dataForAdding.initDate;
    let to = this.dataForAdding.endDate;
    if (to === undefined) {
      to = new Date(new Date().valueOf() - (60 * 60 * 1000));
    }
    this.search(from, to, true, position + 1);
  }

  public search(from: string, to: string, append: boolean = false, position: number = -1) {
    this.generateCopyUrl(from, to);
    if (!append) {
      this.showGrid = false;
      this.waiting = true;
      this.rowData = [];
    }
    this.showError = false;
    this.showClearData = true;
    // All variables (boolean) have a default value as true
    // The search will be on  hosts + message + componentType

    let types: Array<string> = [];

    // if (this.sutlogsType) {
    //   types.push('sutlogs');
    // }

    // if (this.testlogsType) {
    //   types.push('testlogs');
    // }

    //Filter only testlogs type
    types.push('testlogs');

    let levels: Array<string> = [];

    if (this.debugLevel) {
      levels.push('debug');
    }

    if (this.infoLevel) {
      levels.push('info');
    }

    if (this.warnLevel) {
      levels.push('warn');
    }

    if (this.errorLevel) {
      levels.push('error');
    }

    // Use clusterName
    if (this.clusterName === undefined || this.clusterName === '') {
      this.clusterName = '*';
    }

    let queryfrom: any;
    let queryto: any;
    let sort: 'asc' | 'desc';

    queryfrom = from;
    if (!this.useTail) {
      queryto = to;
      sort = 'asc';
      this.showLoadMore = true;
      this.showPauseTail = false;
      if (this.tailInterval) {
        clearInterval(this.tailInterval);
      }
      this.tailInterval = undefined;
    } else {
      queryto = 'now';
      queryfrom = dateToInputLiteral(new Date(new Date().valueOf() - (2 * 60 * 61 * 1000)));
      sort = 'asc';
      this.showLoadMore = false;
      this.showPauseTail = true;
      if (this.tailInterval === undefined) {
        this.tailSearch(true);
      }
    }

    let queries: any = {
      'indices': {
        'indices': [],
        'query': {
          'bool': {
            'filter': {
              'bool': {
                'must': [
                  {
                    'range': {
                      '@timestamp': {
                        'gte': queryfrom,
                        'lte': queryto
                      }
                    }
                  }
                ]
              }
            }
          }
        },
        'no_match_query': 'none'
      }
    };

    let index_: string = '';

    if (this.indexName === undefined || this.indexName === '') {
      if (!this.useTail) {
        let today = dateToInputLiteral(new Date(new Date().valueOf()));
        let differenceFromAndToday = this.getDifferenceDates(from, today);
        let differenceTodayAndTo = this.getDifferenceDates(today, to);

        for (var i = differenceFromAndToday; i >= differenceTodayAndTo; i--) {
          let date = new Date();
          date.setDate(date.getDate() - i);
          index_ = 'kurento-' + this.clusterName + '-' + dateToInputLiteral(date).split('T')[0].replace(/-/g, '.');
          queries.indices.indices.push(index_);
        }
      } else {
        let today = dateToInputLiteral(new Date(new Date().valueOf()));
        index_ = 'kurento-' + this.clusterName + '-' + today.split('T')[0].replace(/-/g, '.');
        queries.indices.indices.push(index_);
      }
    } else {
      index_ = this.indexName;
      queries.indices.indices.push(index_);
    }

    let url = this.urlElastic + '_search?scroll=1m&filter_path=_scroll_id,hits.hits._source,hits.hits._type,hits';

    console.log('URL:', url);

    this.addTermFilter(queries, 'level', levels);
    this.addTermFilter(queries, '_type', types);

    let hosts = this.processCommaSeparatedValue(this.hosts);
    this.addTermFilter(queries, 'host', hosts);

    let message = this.processCommaSeparatedValue(this.message);
    this.addTermFilter(queries, 'message', message);

    let componentType = this.processCommaSeparatedValue(this.componentType);
    this.addTermFilter(queries, 'component_type', componentType);

    console.log('Query: ', JSON.stringify(queries));
    console.log('-----------------------------------------------------------------');

    let theQuery = {
      sort: [
        { '@timestamp': sort }
      ],
      query: queries,
      size: this.maxResults,
      highlight: {
        pre_tags: ['<b><i>'],
        post_tags: ['</i></b>'],
        fields: {
          message: { number_of_fragments: 0 },
          host: { number_of_fragments: 0 },
          componentType: { number_of_fragments: 0 },
          level: { number_of_fragments: 0 },
          logmessage: { number_of_fragments: 0 }
        }
      },
      _source: ['host', 'component_type', 'message', 'level', 'logmessage', '@timestamp', 'tjobexec']
    };

    if (!append) {
      this.rowData = [];
    }

    if (append && position === -1) {
      if (!this.noMore) {
        if (this.rowData.length > 0) {
          url = this.urlElastic + '_search/scroll';
          let theQuery = { scroll: '1m', scroll_id: this._scroll_id };
        }
      } else {
        theQuery.query.indices.query.bool.filter.bool.must[0].range['@timestamp'].gte = this.rowData[this.rowData.length - 1].time;
      }
    }

    this._elasticSearchService.internalSearch(url, theQuery, this.maxResults, append).subscribe(
      data => {

        console.log('Data:', data);

        this._scroll_id = data._scroll_id;

        if (data.hits !== undefined && data.hits.hits.length === 0 && this.rowData.length === 0) {
          console.log('Returned response without results. Aborting');
          this.rowData = [];
          this.rowData = this.rowData.slice();
          this.showGrid = true;
          this.waiting = false;
          return;
        }

        let initPosition: number = -1;
        let endPosition: number = -1;

        if (data.hits) {
          console.log('Data hits size:', data.hits.hits.length);
          let prevSize: number = position;
          if (position === -1) {
            prevSize = this.rowData.length;
          }
          this.noMore = data.hits.hits.length === 0;

          var random = Math.floor((Math.random() * 10000) + 1);
          if (position !== -1) {
            initPosition = position;
            while (this.rowData[initPosition].message.indexOf('End Sub-Search') > -1) {
              initPosition++;
              position--;
            }

            let tjobexec = '';
            let type = '';
            let time = '';

            let message = '';
            let level = '';
            let componentType = '';
            let host = '';

            let logValue = { tjobexec, type, time, message, level, componentType, host };
            this.rowData.splice(initPosition, 0, logValue);
            this.rowData = this.rowData.slice();
            position++;
          }

          for (let logEntry of data.hits.hits) {
            let tjobexec = logEntry._source.tjobexec;
            let type = logEntry._type;
            let time = logEntry._source['@timestamp'];
            let message = '';
            if (logEntry._source['message'] !== undefined) {
              message = logEntry._source['message'];
            } else {
              message = 'undefined';
            }
            let level = logEntry._source.level;
            componentType = logEntry._source['component_type'];

            let host = logEntry._source.host;
            if (logEntry.host !== undefined) {
              host = logEntry.host[0];
            }

            let logValue = { tjobexec, type, time, message, level, componentType, host };

            if (append) {
              if (position !== -1) {
                while (this.rowData[position].message.indexOf('Sub-Search') > -1) {
                  position++;
                }
                let positionMessage = this.rowData[position].message.replace('<b><i>', '').replace('</i></b>', '');
                let initPositionMessage =
                  this.rowData[initPosition - 1].message.replace('<b><i>', '').replace('</i></b>', '');
                if ((this.rowData[position] !== undefined && ((this.rowData[position].time === logValue.time
                  && positionMessage === message.replace('<b><i>', '').replace('</i></b>', ''))))) {
                  position++;
                  continue;
                }
                if (this.rowData[initPosition - 1] !== undefined && ((this.rowData[initPosition - 1].time === logValue.time
                  && initPositionMessage === message.replace('<b><i>', '').replace('</i></b>', '')))) {
                  continue;
                }
                this.rowData.splice(position, 0, logValue);
                position++;
              } else {
                if (prevSize === 0) {
                  this.rowData.push(logValue);
                } else {
                  let prevMessage = this.rowData[prevSize - 1].message.replace('<b><i>', '').replace('</i></b>', '');
                  if (this.rowData[prevSize - 1].time === logValue.time &&
                    prevMessage === message.replace('<b><i>', '').replace('</i></b>', '')) {
                    continue;
                  }
                  this.rowData.push(logValue);
                }
              }
            } else {
              this.rowData.push(logValue);
            }
          }
          if (position !== -1) {
            if (position - initPosition === 0) {
              endPosition = initPosition + 1;
            } else if (position - initPosition === 2) {
              endPosition = position - 1;
            } else {
              endPosition = position;
              while (this.rowData[endPosition].message.indexOf('Sub-Search') > -1) {
                endPosition++;
              }
            }
            let tjobexec = '';
            let type = '';
            let time = '';
            let message = 'End Sub-Search: ' + random;
            let level = '';
            let componentType = '';
            let host = '';

            let logValue = { tjobexec, type, time, message, level, componentType, host };
            this.rowData.splice(endPosition, 0, logValue);
          }
          if (data.hits.hits.length > 0) {
            this.rowData = this.rowData.slice();
          }
        }

        this.showGrid = true;
        this.waiting = false;
      },
      err => {
        console.log('Error', err);
        this.errorMsg = err._body;
        this.showError = true;
        this.waiting = false;
        this.clearData();
      }
    );
  }

}
