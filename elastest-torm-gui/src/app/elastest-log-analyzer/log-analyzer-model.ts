export class LogAnalyzerModel {
    selectedIndices: string[];
    fromDate: Date;
    toDate: Date;
    maxResults: number;
    tail: boolean;

    constructor() {
        this.selectedIndices = ['*'];
        this.fromDate = this.getDefaultFromDate();
        this.toDate = this.getDefaultToDate();
        this.maxResults = 500;
        this.tail = false;
    }

    public getDefaultFromDate(): Date {
        return new Date(new Date().valueOf() - (24 * 60 * 60 * 1000));
    }

    public getDefaultToDate(): Date {
        return new Date(new Date().valueOf() + (2 * 60 * 60 * 1000));
    }

    public selectedIndicesToString(): string {
        return this.selectedIndices.join(',');
    }

}