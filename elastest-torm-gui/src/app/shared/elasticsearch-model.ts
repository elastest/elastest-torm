export class ESTermsModel {
    name: string;
    values: string[];

    constructor() {
        this.name = '';
        this.values = [];
    }

    empty(): boolean {
        return this.name === '' || this.values.length === 0;
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted = { terms: {} };
            formatted.terms[this.name] = this.values;
        }
        return formatted;
    }
}
export class ESTermModel {
    name: string;
    value: string;

    constructor() {
        this.name = '';
        this.value = '';
    }

    empty(): boolean {
        return this.name === '' || this.value === '';
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted = { term: {} };
            formatted.terms[this.name] = this.value;
        }
        return formatted;
    }
}
export class ESRangeModel {
    field: string;
    gte: any;
    gt: any;
    lte: any;
    lt: any;
    boost: any;
    others: { // Can be used to set not generic fileds like:  "format": "dd/MM/yyyy||yyyy" (view ES Range docs)
        label: string,
        value: string,
    }[];

    constructor() {
        this.field = '';
        this.gte = '';
        this.gt = '';
        this.lte = '';
        this.lt = '';
        this.boost = '';
        this.others = [];
    }

    empty(): boolean {
        return (this.field === '') ||
            (
                this.field !== '' && this.gte === '' && this.gt === ''
                && this.lte === '' && this.lt === '' && this.boost === '' && this.others.length === 0
            );
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted = { range: {} };
            formatted.range[this.field] = {
                gte: this.gte,
                gt: this.gt,
                lte: this.lte,
                lt: this.lt,
                boost: this.boost,
            };
            for (let other of this.others) {
                if (other.label !== '' && other.value !== '') {
                    formatted.range[other.label] = other.value;
                }
            }
        }
        return formatted;
    }
}
export class ESBoolClausesModel {
    range: ESRangeModel;
    termList: ESTermModel[];
    termsList: ESTermsModel[];

    constructor() {
        this.range = new ESRangeModel();
        this.termList = [];
        this.termsList = [];
    }

    empty(): boolean {
        return this.range.empty() && this.termList.length === 0 && this.termsList.length === 0;
    }

    convertToESFormat(): any[] {
        let formatted: any[] = [];
        if (!this.empty()) {
            if (!this.range.empty()) {
                formatted.push(this.range.convertToESFormat());
            }

            for (let term of this.termList) {
                formatted.push(term.convertToESFormat());
            }

            for (let terms of this.termsList) {
                formatted.push(terms.convertToESFormat());
            }
        }
        return formatted;
    }

}
export class ESBoolModel {
    must: ESBoolClausesModel;
    filter: ESBoolClausesModel;
    should: ESBoolClausesModel;
    mustNot: ESBoolClausesModel;

    constructor() {
        this.must = new ESBoolClausesModel();
        this.filter = new ESBoolClausesModel();
        this.should = new ESBoolClausesModel();
        this.mustNot = new ESBoolClausesModel();
    }

    empty(): boolean {
        return this.must.empty() && this.filter.empty() && this.should.empty() && this.mustNot.empty();
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted.must = this.must.convertToESFormat();
            formatted.filter = this.filter.convertToESFormat();
            formatted.should = this.should.convertToESFormat();
            formatted.mustNot = this.mustNot.convertToESFormat();
        }
        return formatted;
    }
}
export class ESQueryModel {
    bool: ESBoolModel;

    // TODO others attrs

    constructor() {
        this.bool = new ESBoolModel();
    }

    empty(): boolean {
        return this.bool.empty();
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted.bool = this.bool.convertToESFormat();
        }
        return formatted;
    }
}
export class ESSearchBodyModel {
    size: number;
    query: ESQueryModel;

    constructor() {
        this.size = -1;
        this.query = new ESQueryModel();
    }

    empty(): boolean {
        return this.size === -1;
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted.size = this.size;
            formatted.query = this.query.convertToESFormat();
        }
        return formatted;
    }
}

export class ESSearchModel {
    indices: string[];
    filterPath: string[];
    searchPath: string;
    body: ESSearchBodyModel;

    constructor() {
        this.indices = [];
        this.filterPath = [];
        this.searchPath = '/_search';
        this.body = new ESSearchBodyModel();
    }

    empty(): boolean {
        return this.indices.length === 0 || !this.searchPath.startsWith('/_search');
    }

    addFiltersPathToSearchUrl(searchUrl: string) {
        searchUrl += '?ignore_unavailable'; // For multiple index (ignore if not exist)
        if (this.filterPath && this.filterPath.length > 0) {
            let filterPathPrefix: string = 'filter_path=';
            let filterPathSource: string = 'hits.hits._source.';
            let filterPathSort: string = 'hits.hits.sort,';

            searchUrl += '&' + filterPathPrefix + filterPathSort;
            let counter: number = 0;
            for (let filter of this.filterPath) {
                searchUrl += filterPathSource + filter;
                if (counter < this.filterPath.length - 1) {
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

    formatIndices() {
        return this.indices.join(',');
    }
}