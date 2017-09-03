export class EsmServiceModel {
    id: number;
    name: string;
    selected: boolean;

    constructor(id: number, name: string, selected: boolean){
        this.id = id;
        this.name = name;
        this.selected =selected;
    }

    changeServiceSelection($event) {        
        this.selected = $event.checked;
    }
    
}
