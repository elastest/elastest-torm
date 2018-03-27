import { query } from '@angular/animations';
import { ESBoolModel } from './es-bool-model';



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
            formatted.term[this.name] = this.value;
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
    boost: number;
    others: { // Can be used to set not generic fileds like:  "format": "dd/MM/yyyy||yyyy" (view ES Range docs)
        label: string,
        value: string,
    }[];

    constructor() { // null is a valid ES value fot gt, gte, lt and lte
        this.field = '';
        this.gte = undefined;
        this.gt = undefined;
        this.lte = undefined;
        this.lt = undefined;
        this.boost = 1.0;
        this.others = [];
    }

    empty(): boolean {
        return (this.field === '') ||
            (
                this.field !== '' && !this.gte && !this.gt && !this.lte && !this.lt && this.others.length === 0
            );
    }

    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted = { range: {} };
            formatted.range[this.field] = { boost: this.boost };
            if (this.gte) { formatted.range[this.field].gte = this.gte; }
            if (this.gt) { formatted.range[this.field].gt = this.gt; }
            if (this.lte) { formatted.range[this.field].lte = this.lte; }
            if (this.lt) { formatted.range[this.field].lt = this.lt; }

            for (let other of this.others) {
                if (other.label !== '' && other.value !== '') {
                    formatted.range[other.label] = other.value;
                }
            }
        }
        return formatted;
    }
}


export type MatchOperators = 'and' | 'or';
export type MatchZeroTermsQuery = 'all' | 'match_all';
export type MultiMatchType = 'phrase' | 'phrase_prefix' | 'best_fields' | 'most_fields' | 'cross_fields';
export type MatchType = 'boolean' | 'phrase' | 'phrase_prefix';
export class ESMatchModel {
    field: string;
    query: string;
    operator: MatchOperators;
    zeroTermsQuery: MatchZeroTermsQuery;
    type: MatchType;

    constructor() {
        this.field = '';
        this.query = '';
        this.operator = undefined;
        this.zeroTermsQuery = undefined;
        this.type = undefined;
    }

    empty(): boolean {
        return (this.field === '' || this.query === '');
    }
    convertToESFormat(): any {
        let formatted: any = {};
        if (!this.empty()) {
            formatted[this.field] = {};
            formatted[this.field].query = this.query;
            if (this.operator) { formatted[this.field].operator = this.operator; }
            if (this.zeroTermsQuery) { formatted[this.field]['zero_terms_query'] = this.zeroTermsQuery; }
        }
        return formatted;
    }
}

export class ESBoolQueryModel {
    bool: ESBoolModel;

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

export class ESQueryModel { // TODO
    convertToESFormat(): any {
        return {};
    }
}