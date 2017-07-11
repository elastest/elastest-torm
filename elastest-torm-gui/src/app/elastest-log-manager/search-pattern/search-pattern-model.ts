export class SearchPatternModel {
    color: string;
    searchValue: string;
    results: number[];
    position: number;

    constructor(){
        this.color = '#ff0000';
        this.searchValue = '';
        this.results = [];
        this.position = -1;
    }
}
