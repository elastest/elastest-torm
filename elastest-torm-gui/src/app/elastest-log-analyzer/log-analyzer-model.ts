import { AgTreeCheckModel, TreeCheckElementModel } from '../shared/ag-tree-model';
export class LogAnalyzerModel {
    // Basic
    selectedIndices: string[];
    fromDate: Date;
    toDate: Date;
    maxResults: number;
    tail: boolean;

    // Filters
    components: AgTreeCheckModel;
    level: string[];

    constructor() {
        this.selectedIndices = ['*'];
        this.fromDate = this.getDefaultFromDate();
        this.toDate = this.getDefaultToDate();
        this.maxResults = 500;
        this.tail = false;

        this.components = new AgTreeCheckModel();
        this.level = [];
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

    public setComponents(rows: any[]): void {
        const components: string[] = Array.from(
            new Set(
                rows.map(
                    (item: any) => item.component,
                ),
            )
        );

        this.components = new AgTreeCheckModel();
        for (let component of components) {
            let treeComponent: TreeCheckElementModel = new TreeCheckElementModel();
            treeComponent.name = component;
            this.components.tree.push(treeComponent);
        }
    }

}