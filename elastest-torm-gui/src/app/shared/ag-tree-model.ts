// Interface
import { TreeNode } from 'angular-tree-component/dist/models/tree-node.model';

export interface ITreeElementModel {
  name: string;
  children: ITreeElementModel[];
  setByObj(elem: any): void;
}

export interface IAgTreeModel {
  tree: ITreeElementModel[];
  setByObjArray(elemArray: any[]): void;
  empty(): boolean;
}

// Implementations

export class TreeCheckElementModel implements ITreeElementModel {
  name: string;
  children: TreeCheckElementModel[];
  checked: boolean;

  constructor() {
    this.name = '';
    this.children = [];
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

  setCheckedToAll(checked: boolean): void {
    this.checked = checked;
    for (let child of this.children) {
      child.setCheckedToAll(checked);
    }
  }
}

export class AgTreeCheckModel implements IAgTreeModel {
  tree: TreeCheckElementModel[];

  constructor() {
    this.tree = [];
  }

  public setCheckedToAll(checked: boolean): void {
    for (let child of this.tree) {
      child.setCheckedToAll(checked);
    }
  }

  public check(node: TreeNode, checked: boolean): void {
    this.updateChildNodeCheckbox(node, checked);
    this.updateParentNodeCheckbox(node.realParent);
  }
  public updateChildNodeCheckbox(node: TreeNode, checked: boolean): void {
    node.data.checked = checked;
    if (node.children) {
      node.children.forEach((child) => this.updateChildNodeCheckbox(child, checked));
    }
  }
  public updateParentNodeCheckbox(node: TreeNode): void {
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

  updateCheckboxes(nodes: TreeNode[]): void {
    for (let node of nodes) {
      if (node.children.length > 0) {
        this.updateCheckboxes(node.children);
      } else {
        this.check(node, node.data.checked);
      }
    }
  }

  public setByObjArray(elemArray: any[]): void {
    for (let elem of elemArray) {
      let newElem: TreeCheckElementModel = new TreeCheckElementModel();
      newElem.setByObj(elem);
      this.tree.push(newElem);
    }
  }

  public empty(): boolean {
    return this.tree.length === 0;
  }

  public getOnlyCheckedTree(tree: TreeCheckElementModel[] = this.tree): TreeCheckElementModel[] {
    let checkedTree: TreeCheckElementModel[] = [];

    for (let node of tree) {
      if (node.checked) {
        let newElem: TreeCheckElementModel = new TreeCheckElementModel();
        newElem.name = node.name;
        newElem.checked = node.checked;
        newElem.children = this.getOnlyCheckedTree(node.children);
        checkedTree.push(newElem);
      }
    }

    return checkedTree;
  }
}
