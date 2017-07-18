import { Component, OnInit, Output, EventEmitter, Inject, ElementRef, ViewChild } from '@angular/core';

import { SearchPatternModel } from './search-pattern/search-pattern-model';
import { ElasticSearchService } from '../shared/services/elasticsearch.service';
import { dateToInputLiteral } from './utils/Utils';
import { Router } from '@angular/router';
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent, ITdDataTableColumn } from '@covalent/core';
import { IPageChangeEvent } from '@covalent/core';


@Component({
  selector: 'app-elastest-log-manager',
  templateUrl: './elastest-log-manager.component.html',
  styleUrls: ['./elastest-log-manager.component.scss'],
})
export class ElastestLogManagerComponent implements OnInit {
  @ViewChild('copyTextArea') copyTextArea: ElementRef;

  public noMore: boolean = false;
  public dataForAdding: any = undefined;
  public onlyTable: boolean = false;
  public goToLogManager: string;

  public indices = [];
  public defaultFrom = new Date(new Date().valueOf() - (10 * 60 * 60 * 1000));
  public defaultTo = new Date(new Date().valueOf() + (2 * 60 * 60 * 1000));

  // show/hide the grid and spinner
  public rowData: any[] = [];
  public showGrid: boolean;
  public showError: boolean;
  public waiting: boolean = false;

  public errorMsg: string = '';

  public sutlogsType: boolean = true;
  public testlogsType: boolean = true;

  public debugLevel: boolean = false;
  public infoLevel: boolean = false;
  public warnLevel: boolean = false;
  public errorLevel: boolean = false;

  public useTail: boolean = false;

  public urlElastic: string = 'http://localhost:9200/';
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


  //Search Table parameters

  emptyTableTextDefault: string = "No results to display.";
  emptyTableText: string = this.emptyTableTextDefault;
  filteredData: any[] = this.rowData;
  filteredTotal: number = this.rowData.length;

  searchTerm: string = '';
  // sortByDefault: string = 'time';
  // sortBy: string = this.sortByDefault;

  //Search table Pagination params. (DISABLED)
  fromRow: number = 1;
  currentPage: number = 1;
  pageSizeDefault: number = 200;
  pageSize: number = this.pageSizeDefault;
  selectedRows: any[] = [];
  // sortOrderDefault: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
  // sortOrder: TdDataTableSortingOrder = this.sortOrderDefault;
  pageSizesListDefault: number[] = [5, 10, 15, 20, 40, 100, 200];
  pageSizesList: number[] = this.pageSizesListDefault;

  currentRowSelected: number = -1;
  currentPos: number = -1;

  searchAfter: number = undefined;

  //Filter Results
  patternDefault: SearchPatternModel = new SearchPatternModel();
  patterns: SearchPatternModel[] = [this.patternDefault];

  columns: any[] = [
    { name: 'time', label: 'Time' },
    { name: 'message', label: 'Message' },
    { name: 'level', label: 'Level' },
    { name: 'type', label: 'Type' },
    { name: 'componentType', label: 'Component Type' },
    { name: 'host', label: 'Host' },
  ];

  constructor(public _elasticSearchService: ElasticSearchService, public router: Router,
    private _dataTableService: TdDataTableService,
  ) {
    let params: any = router.parseUrl(router.url).queryParams;

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

    if (params.sutlogsType !== undefined && params.sutlogsType !== null && params.sutlogsType === 'false') {
      autoSearch = true;
      this.sutlogsType = false;
    } else if (params.sutlogsType === 'true') {
      autoSearch = true;
      this.sutlogsType = true;
    }

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
      this.defaultFrom = new Date(params.from);
    }

    if (params.to !== undefined && params.to !== null) {
      autoSearch = true;
      this.defaultTo = new Date(params.to);
    }

    if (autoSearch) {
      this.search(dateToInputLiteral(this.defaultFrom), dateToInputLiteral(this.defaultTo));
    }

    let url = this.urlElastic + '_mapping';
  }


  ngOnInit() {
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
      queryes.bool.must.push({
        'terms': filter
      });
    } else if (values.length === 1) {
      if (field === 'message') {
        let filterValue = '{\"multi_match\": {\"query\" : \"' + values.join(' ') +
          '\",\"type\": \"phrase\", \"fields\": [\"message\", \"logmessage\"] }}';
        queryes.bool.must.push(JSON.parse(filterValue));
      } else {
        filter[field] = values[0];
        let filterValue = '{\"match\":{\"' + field + '\" : {\"query\" : \"' + values.join(' ') +
          '\",\"type\": \"phrase\" }}}';
        queryes.bool.must.push(JSON.parse(filterValue));
      }
    }
  }

  public copyToClipboard() {
    this.copyTextArea.nativeElement.select();
    document.execCommand('copy');
  }

  public generateCopyUrl(from: string, to: string) {
    this.urlCopied = location.protocol + '//' + location.host + location.pathname + '?';

    if (this.urlElastic !== undefined) {
      this.urlCopied += 'urlElastic=' + encodeURIComponent(this.urlElastic) + '&';
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
  public updateUrlElastic(event: Event): void {
    var value: string;
    if (event === undefined) {
      value = this.urlElastic;
    } else {
      value = (<HTMLSelectElement>event.srcElement).value;
    }
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
    this.filteredData = [];
    clearInterval(this.tailInterval);
    this.tailInterval = undefined;
    this.showGrid = false;
    this.showLoadMore = false;
    this.showPauseTail = false;
    this.showClearData = false;
    // this.sortBy = this.sortByDefault;
    // this.sortOrder = this.sortOrderDefault;
    this.removeAllPatterns();
    this.addPattern();
  }

  // Used in html file
  public addMore() {
    if (this.currentRowSelected >= 0 && this.dataForAdding !== undefined) {
      let from = this.dataForAdding.initDate;
      let to = this.dataForAdding.endDate;
      if (to === undefined) {
        to = new Date(new Date().valueOf() - (60 * 60 * 1000));
      }
      this.search(from, to, true, true);
    }
  }

  public search(from: string, to: string, append: boolean = false, fromData: boolean = false) {
    this.emptyTableText = "Searching...";
    this.generateCopyUrl(from, to);
    if (!append) {
      this.waiting = true;
      this.clearData();
    }
    this.showError = false;
    this.showClearData = true;

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
    };

    let url = this.urlElastic + this.indexName + '/_search?&filter_path=hits.hits._source,hits.hits._type,hits';

    console.log('URL:', url);

    this.applyFilters(queries);

    console.log('Query: ', JSON.stringify(queries));

    let theQuery = {
      sort: [
        { '@timestamp': sort }
      ],
      query: queries,
      size: this.maxResults,
      _source: ['host', 'component_type', 'message', 'level', 'logmessage', '@timestamp', 'tjobexec']
    };

    if (!append) {
      this.rowData = [];
    }

    if (append) { //Not new search
      if (!fromData) { //Load more
        if (!this.noMore) {
          if (this.rowData.length > 0 && this.searchAfter !== undefined) {
            theQuery['search_after'] = [this.searchAfter];
          }
        } else {
          theQuery.query.indices.query.bool.filter.bool.must[0].range['@timestamp'].gte = this.rowData[this.rowData.length - 1].time;
        }
      }
      else { //Add more from row
        theQuery['search_after'] = [this.dataForAdding.searchAfter];
      }
    }

    this._elasticSearchService.internalSearch(url, theQuery, this.maxResults, append)
      .finally(
      () => { this.emptyTableText = this.emptyTableTextDefault; }
      )
      .subscribe(
      (data: any) => {
        console.log('Data:', data);

        let dataReceived: number = data.hits.hits.length;
        if (dataReceived > 0) {
          let lastReceivedPos: number = dataReceived - 1;
          this.searchAfter = data.hits.hits[lastReceivedPos].sort[0];
        }
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

          this.noMore = data.hits.hits.length === 0;
          this.parseSearchedData(data, fromData); //Set data into table rows

          //Update table
          this.initSearchTable(1, 1, this.rowData.length);
        }
        this.emptyTableText = this.emptyTableTextDefault;

        this.showGrid = true;
        this.waiting = false;
      },
      err => {
        console.log('Error', err);
        this.errorMsg = err._body;
        this.showError = true;
        this.waiting = false;
        this.clearData();
        this.emptyTableText = this.emptyTableTextDefault;
      }
      );
  }

  applyFilters(queries: any) {
    let types: Array<string> = [];
    if (this.sutlogsType) {
      types.push('sutlogs');
    }

    if (this.testlogsType) {
      types.push('testlogs');
    }

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

    this.addTermFilter(queries, 'level', levels);
    this.addTermFilter(queries, '_type', types);

    let hosts = this.processCommaSeparatedValue(this.hosts);
    this.addTermFilter(queries, 'host', hosts);

    let message = this.processCommaSeparatedValue(this.message);
    this.addTermFilter(queries, 'message', message);

    let componentType = this.processCommaSeparatedValue(this.componentType);
    this.addTermFilter(queries, 'component_type', componentType);
  }

  /**
   * Parse elasticsearch data and save rows into table array
   * @param data
   */
  parseSearchedData(data: any, fromData: boolean = false) {
    let tjobexec: string = '';
    let type: string = '';
    let time: string = '';
    let message: string = '';
    let level: string = '';
    let componentType: string = '';
    let host: string = '';
    let sortId: number = undefined;

    let logRow: any;
    let position: number;
    let newPosition: number = fromData ? this.dataForAdding.position + 1 : undefined;

    for (let logEntry of data.hits.hits) {
      tjobexec = logEntry._source.tjobexec;
      type = logEntry._type;
      time = logEntry._source['@timestamp'];
      message = '';
      if (logEntry._source['message'] !== undefined) {
        message = logEntry._source['message'];
      } else {
        message = 'undefined';
      }
      level = logEntry._source.level;
      componentType = logEntry._source['component_type'];

      host = logEntry._source.host;
      if (logEntry.host !== undefined) {
        host = logEntry.host[0];
      }

      sortId = logEntry.sort[0];

      if (!fromData) { // New search or Load More
        position = this.rowData.length;
        logRow = { tjobexec, type, time, message, level, componentType, host, sortId, position };
        this.rowData.push(logRow);
      }
      else { // Add from row selected
        position = newPosition;
        logRow = { tjobexec, type, time, message, level, componentType, host, sortId, position };
        this.rowData.splice(position, 0, logRow);
        newPosition++;
      }
    }

    if (fromData && data.hits !== undefined && data.hits.hits.length > 0) {
      this.updateRowsPositions(newPosition);
    }
  }

  updateRowsPositions(fromPosition: number) {
    for (let i = fromPosition; i < this.rowData.length; i++) {
      this.rowData[i].position = i;
    }
  }


  // Table Search functions

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    // this.sortBy = sortEvent.name;
    // this.sortOrder = sortEvent.order;
    this.filter();
  }

  searchTable(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.filter();
  }

  initSearchTable(fromRow: number, page: number, pageSize: number) {
    this.fromRow = fromRow;
    this.currentPage = page;
    this.pageSize = pageSize;
    this.filter();
  }

  page(pagingEvent: IPageChangeEvent): void {
    this.fromRow = pagingEvent.fromRow;
    this.currentPage = pagingEvent.page;
    this.pageSize = pagingEvent.pageSize;
    this.filter();
  }

  filter(): void {
    let newData: any[] = this.rowData;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    // newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  onRowClicked($event) {
    if ($event.row !== undefined) {
      console.log($event.row)
      let rows: NodeListOf<HTMLTableRowElement> = this.getSearchTableRows();
      if (rows !== undefined && rows !== null) {
        if (this.currentRowSelected >= 0) { // Clear previous selected row color
          rows[this.currentRowSelected].bgColor = '';
        }
        rows[$event.row.position].bgColor = '#ffac2f';
      }
      this.currentRowSelected = $event.row.position;
      console.log('selected', this.currentRowSelected)
      let initDate: String = this.rowData[$event.row.position].time;
      let endDate: String;
      let i: number = 1;
      do {
        let endRow = this.rowData[$event.row.position + i];
        if (endRow !== undefined) {
          endDate = endRow.time;
        }
        if (endDate === '') {
          endDate = undefined;
        }
        i++;
      } while (i < this.rowData.length && endDate === undefined);

      let event = {
        position: $event.row.position,
        searchAfter: $event.row.sortId,
        initDate: initDate,
        endDate: endDate
      };
      this.updateDatesForMoreDate(event);
    }
  }



  // Filter results functions
  addPattern() {
    this.patterns.push(new SearchPatternModel());
  }

  removePattern(position: number) {
    this.patterns.splice(position, 1);
  }

  clearPatterns() {
    for (let pattern of this.patterns) {
      pattern.searchValue = '';
      pattern.results = [];
    }
    this.currentPos = -1;
    this.currentRowSelected = -1;
    this.cleanRowsColor();
  }

  removeAllPatterns() {
    this.patterns = [];
    this.currentPos = -1;
    this.currentRowSelected = -1;
    this.cleanRowsColor();
  }

  cleanRowsColor() {
    let rows: NodeListOf<HTMLTableRowElement> = this.getSearchTableRows();
    if (rows !== undefined && rows !== null) {
      let i: number = 0;
      while (rows[i] !== undefined && rows[i] !== null) {
        rows[i].removeAttribute('style');
        i++;
      }
    }
  }

  searchByPatterns() {
    this.currentPos = -1;
    this.cleanRowsColor();
    let i: number = 0;
    this.filteredData.map(e => {
      for (let pattern of this.patterns) {
        if ((pattern.searchValue !== '') && (e.message.toUpperCase().indexOf(pattern.searchValue.toUpperCase()) > -1)) {
          if (pattern.results.indexOf(i) === -1) {
            pattern.results.push(i);
          }
        }
      }
      i++;
    });

    let j: number = 0;
    for (let pattern of this.patterns) {
      if (pattern.results.length > 0) {
        this.next(j);
        this.paintResults(j);
      }
      j++;
    }
  }

  paintResults(index: number) {
    let rows: NodeListOf<HTMLTableRowElement> = this.getSearchTableRows();
    if (rows !== undefined && rows !== null) {
      for (let result of this.patterns[index].results) {
        rows[result].style.color = this.patterns[index].color;
      }
    }
  }


  next(index: number) {
    let pattern: SearchPatternModel = this.patterns[index];
    if (pattern.results.length > 0) {
      pattern.results.sort(this.sorted);

      if (this.currentPos === -1) {
        pattern.position = 0;
      } else {
        pattern.position = this.getNextPosition(this.currentPos, pattern.results);
        if (pattern.position === -1) {
          pattern.position = 0;
        }
      }
      this.focusRow(pattern.results[pattern.position]);
    }
  }


  prev(index: number) {
    let pattern: SearchPatternModel = this.patterns[index];
    if (pattern.results.length > 0) {
      pattern.results.sort(this.sorted);

      if (this.currentPos === -1) {
        pattern.position = pattern.results.length - 1;
      } else {
        pattern.position = this.getPrevPosition(this.currentPos, pattern.results);
        if (pattern.position === -1) {
          console.log('resu', pattern.results, 'pos', pattern.results.length - 1)
          pattern.position = pattern.results.length - 1;
        }
      }
      this.focusRow(pattern.results[pattern.position]);
    }
  }

  focusRow(newPos: number) {
    let previousPos: number = this.currentPos;
    this.currentPos = newPos;
    let rows: NodeListOf<HTMLTableRowElement> = this.getSearchTableRows();
    if (rows !== undefined && rows !== null) {
      if (previousPos >= 0) {
        rows[previousPos].style.removeProperty('background-color');
      }
      rows[this.currentPos].style.backgroundColor = '#e0e0e0';
      rows[this.currentPos].focus();
    }
  }

  getNextPosition(element: number, array: Array<number>) {
    let i: number;
    for (i = 0; i < array.length; i++) {
      if (element < array[i]) {
        return i;
      }
    }
    return -1;
  }

  getPrevPosition(element: number, array: Array<number>) {
    let i: number;
    for (i = array.length; i >= 0; i--) {
      if (element > array[i]) {
        return i;
      }
    }
    return -1;
  }

  sorted(a: number, b: number) {
    return a - b;
  }

  getSearchTableRows() {
    if (document.getElementById('dataTable') !== null && document.getElementById('dataTable') !== undefined) {
      return document.getElementById('dataTable').getElementsByTagName('tbody')[0].getElementsByTagName('tr');
    }
    else {
      return undefined;
    }
  }
}