import { ESTermModel } from '../shared/elasticsearch-model';
import { AgTreeCheckModel, TreeCheckElementModel } from '../shared/ag-tree-model';
export class LogAnalyzerModel {
    // Basic
    selectedIndices: string[];
    fromDate: Date;
    toDate: Date;
    maxResults: number;
    tail: boolean;

    // Filters
    componentsStreams: AgTreeCheckModel;
    levels: AgTreeCheckModel;
    messageFilter: string;

    constructor() {
        this.selectedIndices = ['*'];
        this.fromDate = this.getDefaultFromDate();
        this.toDate = this.getDefaultToDate();
        this.maxResults = 800;
        this.tail = false;

        this.componentsStreams = new AgTreeCheckModel();
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

    public setComponentsStreams(componentsStreams: any[]): void {
        this.componentsStreams.setByObjArray(componentsStreams);
        this.componentsStreams.setCheckedToAll(true);
    }

    public setLevels(levels: any[]): void {
        this.levels.setByObjArray(levels);
        this.levels.setCheckedToAll(false);
    }

    public getLevelsTermList(): ESTermModel[] {
        let levelsTerm: ESTermModel[] = [];
        for (let level of this.levels.tree) {
            if (level.checked) {
                let levelTerm: ESTermModel = new ESTermModel();
                levelTerm.name = 'level';
                levelTerm.value = level.name;
                levelsTerm.push(levelTerm);
            }
        }
        return levelsTerm;
    }

}