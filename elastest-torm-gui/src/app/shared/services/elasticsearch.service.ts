import { ConfigModel } from '../../config/config-model';
import { ConfigurationService } from '../../config/configuration-service.service';

import { Injectable } from '@angular/core';
import { Http, Request, RequestMethod, RequestOptions, Response, Headers } from '@angular/http';
import { Subject, Observable } from 'rxjs/Rx';
import 'rxjs/Rx';
import { ESSearchModel } from '../elasticsearch-model/elasticsearch-model';
import { ESBoolModel } from '../elasticsearch-model/es-bool-model';
import { ESTermModel, ESTermsModel, ESRangeModel } from '../elasticsearch-model/es-query-model';

@Injectable()
export class ElasticSearchService {
  public rowData: any[] = [];
  noMore: boolean = false;

  esUrl: string;

  constructor(public http: Http, private configurationService: ConfigurationService) {
    this.rowData = [];
    this.esUrl = this.configurationService.configModel.hostElasticsearch;
  }

  internalSearch(url: string, query: any): Observable<any> {
    // console.log('URL:', url, 'Query:', query);
    let headers: Headers = new Headers();
    headers.append('Content-Type', 'application/json');

    let requestOptions: RequestOptions = new RequestOptions({
      method: RequestMethod.Post,
      headers: headers,
      url,
      body: JSON.stringify(query),
    });

    return this.http.request(new Request(requestOptions)).map(
      (res: Response) => {
        return res.json();
      },
      (err: Response) => {
        console.error('Error:', err);
      },
    );
  }

  /**
   * Search and return all hits recursively by terms and/or given query
   * @param index
   * @param terms
   * @param theQuery optional
   */
  searchAllByTerm(
    index: string,
    terms: any[],
    timeRange?: ESRangeModel,
    theQuery?: any,
    filterPath?: string[],
  ): Observable<string[]> {
    let size: number = 1000;
    let url: string = this.esUrl + index;
    let searchUrl: string = url + '/_search';

    searchUrl = this.addFilterToSearchUrl(searchUrl, filterPath);

    if (theQuery === undefined || theQuery === null) {
      theQuery = this.getDefaultQueryByRawTermList(terms, timeRange);
    }
    theQuery['size'] = size;
    let _traces: Subject<string[]> = new Subject<string[]>();
    let tracesObs: Observable<string[]> = _traces.asObservable();

    this.internalSearch(searchUrl, theQuery).subscribe((data) => {
      if (data.hits && data.hits.hits) {
        let dataReceived: number = data.hits.hits.length;
        if (dataReceived > 0) {
          let lastReceivedPos: number = dataReceived - 1;
          let sortIdList: any[] = data.hits.hits[lastReceivedPos].sort;
          theQuery['search_after'] = sortIdList;

          this.searchAllByTerm(index, terms, timeRange, theQuery, filterPath).subscribe(
            (result) => {
              _traces.next(data.hits.hits.concat(result));
            },
            (error) => console.error(error),
          );
        } else {
          _traces.next([]);
        }
      } else {
        _traces.next([]);
      }
    });

    return tracesObs;
  }

  getDefaultQuery(bool: ESBoolModel): object {
    let esSearchModel: ESSearchModel = new ESSearchModel();
    esSearchModel.body.boolQuery.bool = bool;
    esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
    esSearchModel.body.sort.sortMap.set('_uid', 'asc'); // Sort by _id too to prevent traces of the same millisecond being disordered
    esSearchModel.body.size = 10000;

    return esSearchModel.getSearchBody();
  }

  getDefaultQueryByTermList(terms: ESTermModel[], timeRange?: ESRangeModel): object {
    let bool: ESBoolModel = new ESBoolModel();
    bool.must.termList = terms;

    if (timeRange !== undefined && timeRange !== null) {
      bool.must.range = timeRange;
    }

    return this.getDefaultQuery(bool);
  }

  getDefaultQueryByRawTermList(terms: object[], timeRange?: ESRangeModel): object {
    let termList: ESTermModel[] = [];
    for (let currentRawTerm of terms) {
      let currentTerm: ESTermModel = new ESTermModel();
      if (currentRawTerm['term'] !== undefined) {
        currentRawTerm = currentRawTerm['term'];
      }
      currentTerm.name = Object.keys(currentRawTerm)[0];
      currentTerm.value = currentRawTerm[currentTerm.name];
      termList.push(currentTerm);
    }

    return this.getDefaultQueryByTermList(termList, timeRange);
  }

  addFilterToSearchUrl(searchUrl: string, filterPath?: string[]): string {
    searchUrl += '?ignore_unavailable'; // For multiple index (ignore if not exist)
    if (filterPath && filterPath.length > 0) {
      let filterPathPrefix: string = 'filter_path=';
      let filterPathSource: string = 'hits.hits._source.';
      let filterPathSort: string = 'hits.hits.sort,';

      searchUrl += '&' + filterPathPrefix + filterPathSort;
      let counter: number = 0;
      for (let filter of filterPath) {
        searchUrl += filterPathSource + filter;
        if (counter < filterPath.length - 1) {
          searchUrl += ',';
        }
        counter++;
      }
    }
    return searchUrl;
  }
}
