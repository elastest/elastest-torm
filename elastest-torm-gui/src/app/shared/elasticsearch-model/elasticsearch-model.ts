import { ESBoolQueryModel } from './es-query-model';
export class ESAggsModel {
    name: string;
    field: string;
    aggs: ESAggsModel;

    constructor() {
        this.name = '';
        this.field = '';
        this.aggs = undefined;
    }

    empty(): boolean {
        return (this.name === '' || this.field === '');
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted = { aggs: {} };
            formatted.aggs[this.name] = { terms: {} };
            formatted.aggs[this.name].terms.field = this.field;
            if (this.aggs && !this.aggs.empty()) {
                formatted.aggs[this.name].aggs = this.aggs.convertToESFormat().aggs;
            }
        }
        return formatted;
    }

    initNestedByFieldsList(orderedFieldsList: string[]): void {
        let aggModel: ESAggsModel = this.createNestedAggs(orderedFieldsList);
        if (aggModel) {
            this.name = aggModel.name;
            this.field = aggModel.field;
            this.aggs = aggModel.aggs;
        }
    }

    createNestedAggs(orderedFieldsList: string[]): ESAggsModel {
        let aggModel: ESAggsModel;
        if (orderedFieldsList.length > 0) {
            aggModel = new ESAggsModel();
            let field: string = orderedFieldsList[0];
            aggModel.name = field + 's';
            aggModel.field = field;

            aggModel.aggs = this.createNestedAggs(orderedFieldsList.slice(1));

        }
        return aggModel;
    }
}

export type SortValues = 'asc' | 'desc';

export class ESSortModel {
    sortMap: Map<string, SortValues>;

    constructor() {
        this.sortMap = new Map<string, SortValues>();
    }

    empty(): boolean {
        return this.sortMap.size === 0;
    }

    convertToESFormat(): any[] {
        let formatted: any[] = [];
        if (!this.empty()) {
            this.sortMap.forEach((value: SortValues, key: string) => {
                let sortObj: any = {};
                sortObj[key] = value;
                formatted.push(sortObj);
            });
        }
        return formatted;
    }
}


export class ESSearchBodyModel {
    size: number;
    boolQuery: ESBoolQueryModel;
    sort: ESSortModel;
    searchAfter: string[];

    constructor() {
        this.size = -1;
        this.boolQuery = new ESBoolQueryModel();
        this.sort = new ESSortModel();
        this.searchAfter = [];
    }

    empty(): boolean {
        return this.size === -1;
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted.size = this.size;
            formatted.query = this.boolQuery.convertToESFormat();
            formatted.sort = this.sort.convertToESFormat();
            if (this.searchAfter.length > 0) {
                formatted['search_after'] = this.searchAfter;
            }
        }
        return formatted;
    }
}

export class ESSearchModel {
    private searchPath: string;
    indices: string[];
    filterPathList: string[];
    body: ESSearchBodyModel;

    constructor() {
        this.indices = [];
        this.filterPathList = [];
        this.searchPath = '/_search';
        this.body = new ESSearchBodyModel();
    }

    empty(): boolean {
        return this.indices.length === 0 || !this.searchPath.startsWith('/_search');
    }

    addFiltersPathToSearchUrl(searchUrl: string) {
        searchUrl += '?ignore_unavailable'; // For multiple index (ignore if not exist)
        if (this.filterPathList && this.filterPathList.length > 0) {
            let filterPathPrefix: string = 'filter_path=';
            let filterPathSource: string = 'hits.hits._source.';
            let filterPathSort: string = 'hits.hits.sort,';

            searchUrl += '&' + filterPathPrefix + filterPathSort;
            let counter: number = 0;
            for (let filter of this.filterPathList) {
                searchUrl += filterPathSource + filter;
                if (counter < this.filterPathList.length - 1) {
                    searchUrl += ',';
                }
                counter++;
            }
        }
        return searchUrl;
    }

    getSearchUrl(esUrl: string): string {
        if (!esUrl.endsWith('/')) {
            esUrl += '/';
        }
        esUrl += this.formatIndices();
        esUrl += this.searchPath;
        esUrl = this.addFiltersPathToSearchUrl(esUrl);
        return esUrl;
    }

    getSearchBody(): any {
        let body: any = {};
        body = this.body.convertToESFormat();

        return body;
    }

    formatIndices(): string {
        return this.indices.join(',');
    }

    getSearchPath(): string {
        return this.searchPath;
    }

    // Utils
    getContentListFromRaw(raw: any): any[] {
        let data: any[] = [];
        if (raw && raw.hits && raw.hits.hits) {
            data = raw.hits.hits;
        }
        return data;
    }

    getDataListFromRaw(raw: any, onlySource: boolean = true): any[] {
        let data: any[] = [];
        let sourcesList: any[] = this.getContentListFromRaw(raw);
        for (let elem of sourcesList) {
            if (!onlySource) {
                for (let key in elem) {
                    if (key !== '_source') {
                        elem._source[key] = elem[key]; // Add key and value into source
                    }
                }
            }
            data.push(elem._source);
        }

        return data;
    }
}