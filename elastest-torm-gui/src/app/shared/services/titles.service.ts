import { Meta, Title } from '@angular/platform-browser';
import { Injectable } from '@angular/core';

@Injectable()
export class TitlesService {
    constructor(
        private _titleService: Title, private meta: Meta,
    ) { }

    setHeadTitle(title: string) {
        // Tab Title
        this._titleService.setTitle(title);
    }

    setTopTitle(title: string) {
        // Top Title
        let tag: HTMLMetaElement = this.meta.getTag('name=toptitle');
        if (!tag) {
            this.meta.addTag({ name: 'toptitle', content: title })
        } else {
            tag.content = title;
        }
        document.getElementById('topTitle').innerHTML = this.meta.getTag('name=toptitle').content;
    }

    setHeadAndTopTitle(head: string, top?: string) {
        this.setHeadTitle(head);
        if (!top) {
            top = head;
        }
        this.setTopTitle(top);
    }

    getTitle() {
        return this._titleService;
    }
}