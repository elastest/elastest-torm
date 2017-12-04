import { ESMatchModel, ESRangeModel, ESTermModel, ESTermsModel } from './es-query-model';

export class ESBoolClausesModel {
    range: ESRangeModel;
    termList: ESTermModel[];
    termsList: ESTermsModel[];
    boolList: ESBoolModel[];
    matchList: ESMatchModel[];

    constructor() {
        this.range = new ESRangeModel();
        this.termList = [];
        this.termsList = [];
        this.boolList = [];
        this.matchList = [];
    }

    empty(): boolean {
        return this.range.empty() && this.termList.length === 0
            && this.termsList.length === 0 && this.boolList.length === 0
            && this.matchList.length === 0;
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

            for (let bool of this.boolList) {
                let boolObj: any = {};
                boolObj.bool = bool.convertToESFormat();
                formatted.push(boolObj);
            }

            for (let match of this.matchList) {
                let matchObj: any = {};
                matchObj.match = match.convertToESFormat();
                formatted.push(matchObj);
            }
        }
        return formatted;
    }

    addTermListToTermList(moreTermList: ESTermModel[]): void {
        this.termList = this.termList.concat(moreTermList);
    }

    addTermsListToTermsList(moreTermsList: ESTermsModel[]): void {
        this.termsList = this.termsList.concat(moreTermsList);
    }

    addBoolListToBoolList(moreBoolList: ESBoolModel[]): void {
        this.boolList = this.boolList.concat(moreBoolList);
    }

    addMatchListToMatchList(moreMatchList: ESMatchModel[]): void {
        this.matchList = this.matchList.concat(moreMatchList);
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