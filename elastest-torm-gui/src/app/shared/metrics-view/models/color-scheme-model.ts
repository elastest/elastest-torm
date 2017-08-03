export class ColorSchemeModel {
    domain: string[];
    name: string;
    selectable: boolean;
    group: string;

    constructor() {
        this.domain = [];
        this.name = '';
        this.selectable = undefined;
        this.group = '';
    }
}