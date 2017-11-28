// Basic

export class TreeElementModel {
    name: string;
    children: TreeElementModel[];

    constructor() {
        this.name = '';
        this.children = [];
    }

    setByObj(elem: any): void {
        if (elem.name) {
            this.name = elem.name;
        }
        if (elem.children) {
            for (let child of elem.children) {
                let newChild: TreeElementModel = new TreeElementModel();
                newChild.setByObj(child);
                this.children.push(newChild);
            }
        }
    }
}

export class AgTreeModel {
    tree: TreeElementModel[];

    constructor() {
        this.tree = [];
    }

    public setByObjArray(elemArray: any[]): void {
        for (let elem of elemArray) {
            let newElem: TreeElementModel = new TreeElementModel();
            newElem.setByObj(elem);
            this.tree.push(newElem);
        }
    }
}

// Heritage

export class TreeCheckElementModel extends TreeElementModel {
    checked: boolean;

    constructor() {
        super();
        this.checked = false;
    }

    setByObj(elem: any): void {
        if (elem.name) {
            this.name = elem.name;
        }
        if (elem.children) {
            for (let child of elem.children) {
                let newChild: TreeCheckElementModel = new TreeCheckElementModel();
                newChild.setByObj(child);
                this.children.push(newChild);
            }
        }
        if (elem.checked) {
            this.checked = elem.checked;
        }
    }
}

export class AgTreeCheckModel {
    tree: TreeCheckElementModel[];

    constructor() {
        this.tree = [];
    }

    public check(node, checked): void {
        this.updateChildNodeCheckbox(node, checked);
        this.updateParentNodeCheckbox(node.realParent);
    }
    public updateChildNodeCheckbox(node, checked): void {
        node.data.checked = checked;
        if (node.children) {
            node.children.forEach((child) => this.updateChildNodeCheckbox(child, checked));
        }
    }
    public updateParentNodeCheckbox(node): void {
        if (!node) {
            return;
        }

        let allChildrenChecked: boolean = true;
        let noChildChecked: boolean = true;

        for (const child of node.children) {
            if (!child.data.checked || child.data.indeterminate) {
                allChildrenChecked = false;
            }
            if (child.data.checked) {
                noChildChecked = false;
            }
        }

        if (allChildrenChecked) {
            node.data.checked = true;
            node.data.indeterminate = false;
        } else if (noChildChecked) {
            node.data.checked = false;
            node.data.indeterminate = false;
        } else {
            node.data.checked = true;
            node.data.indeterminate = true;
        }
        this.updateParentNodeCheckbox(node.parent);
    }

    public setByObjArray(elemArray: any[]): void {
        for (let elem of elemArray) {
            let newElem: TreeCheckElementModel = new TreeCheckElementModel();
            newElem.setByObj(elem);
            this.tree.push(newElem);
        }
    }

}