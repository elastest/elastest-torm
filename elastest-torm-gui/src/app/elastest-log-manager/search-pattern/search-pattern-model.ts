export class SearchPatternModel {
    color: string;
    searchValue: string;
    results: number[];
    position: number;
    found: number;

    constructor() {
        this.color = this.generateRandomColor();
        this.searchValue = '';
        this.results = [];
        this.position = -1;
        this.found = -1;
    }

    generateRandomColor() {
        let color: string = '#' + Math.floor(Math.random() * 16777215).toString(16);
        if (color === '#000000' || color === '#ffffff') {
            return this.generateRandomColor();
        }
        return color;
    }
}
