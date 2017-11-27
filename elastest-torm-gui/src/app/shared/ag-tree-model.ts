// Basic

export class TreeElementModel {
    name: string;
    children: TreeElementModel[];

    constructor() {
        this.name = '';
        this.children = [];
    }
}

export class AgTreeModel {
    tree: TreeElementModel[];

    constructor() {
        this.tree = [];
    }
}

// Heritage

export class TreeCheckElementModel extends TreeElementModel {
    checked: boolean;

    constructor() {
        super();
        this.checked = false;
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
}