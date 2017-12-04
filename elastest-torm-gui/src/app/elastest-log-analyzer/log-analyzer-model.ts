import { ESTermModel } from '../shared/elasticsearch-model/es-query-model';
import { Subscription } from 'rxjs/Rx';
import { ESBoolModel } from '../shared/elasticsearch-model/es-bool-model';
import { AgTreeCheckModel, TreeCheckElementModel } from '../shared/ag-tree-model';
export class LogAnalyzerModel {
    // Basic
    selectedIndices: string[];
    fromDate: Date;
    toDate: Date;
    maxResults: number;
    tail: boolean;
    tailSubscription: Subscription;
    pauseTail: boolean;
    usingTail: boolean;
    messageFilter: string;

    selectedRow: number;

    // Filters
    componentsStreams: AgTreeCheckModel;
    levels: AgTreeCheckModel;

    constructor() {
        this.selectedIndices = ['*'];
        this.fromDate = this.getDefaultFromDate();
        this.toDate = this.getDefaultToDate();
        this.maxResults = 800;
        this.tail = false;
        this.tailSubscription = undefined;
        this.pauseTail = false;
        this.usingTail = false;
        this.messageFilter = '';

        this.componentsStreams = new AgTreeCheckModel();
        this.levels = new AgTreeCheckModel();
        this.messageFilter = '';
        this.selectedRow = undefined;
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
        this.componentsStreams = new AgTreeCheckModel();
        this.componentsStreams.setByObjArray(componentsStreams);
        this.componentsStreams.setCheckedToAll(true);
    }

    public setLevels(levels: any[]): void {
        this.levels = new AgTreeCheckModel();
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

    public getComponentsStreamsBool(): ESBoolModel {
        let boolParent: ESBoolModel = new ESBoolModel();
        let boolList: ESBoolModel[] = [];
        for (let componentStream of this.componentsStreams.tree) {
            let component: string = componentStream.name;
            let termComponent: ESTermModel = new ESTermModel();
            termComponent.name = 'component';
            termComponent.value = component;
            for (let stream of componentStream.children) {
                if (stream.checked) {
                    let bool: ESBoolModel = new ESBoolModel();
                    let termStream: ESTermModel = new ESTermModel();
                    termStream.name = 'stream';
                    termStream.value = stream.name;
                    bool.must.termList.push(termComponent, termStream);
                    boolList.push(bool);
                }
            }
        }
        boolParent.should.addBoolListToBoolList(boolList);

        return boolParent;
    }

    public hasSelectedRow(): boolean {
        return this.selectedRow !== undefined;
    }

    public switchPauseTail(pause: boolean): void {
        this.pauseTail = pause;
    }

    public stopTail(): void {
        if (this.tailSubscription) {
            this.tailSubscription.unsubscribe();
        }
        this.tailSubscription = undefined;
    }

}