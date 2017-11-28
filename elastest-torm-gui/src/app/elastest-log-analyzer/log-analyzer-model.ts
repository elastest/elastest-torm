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
    levels: AgTreeCheckModel;
    messageFilter: string;

    constructor() {
        this.selectedIndices = ['*'];
        this.fromDate = this.getDefaultFromDate();
        this.toDate = this.getDefaultToDate();
        this.maxResults = 800;
        this.tail = false;

        this.components = new AgTreeCheckModel();
        this.levels = new AgTreeCheckModel();
        this.messageFilter = '';
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
            treeComponent.checked = true;
            this.components.tree.push(treeComponent);
        }
    }

    public setLevels(rows: any[]): void {
        const levels: string[] = Array.from(
            new Set(
                rows.map(
                    (item: any) => item.level,
                ),
            )
        );

        this.levels = new AgTreeCheckModel();
        for (let level of levels) {
            if (level && level !== '') {
                let treeLevel: TreeCheckElementModel = new TreeCheckElementModel();
                treeLevel.name = level;
                treeLevel.checked = true;
                this.levels.tree.push(treeLevel);
            }
        }
    }

}