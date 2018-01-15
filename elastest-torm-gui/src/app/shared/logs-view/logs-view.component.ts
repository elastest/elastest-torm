import { ElasticSearchService } from '../services/elasticsearch.service';
import { LogViewModel } from './log-view-model';

import { Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'logs-view',
  templateUrl: './logs-view.component.html',
  styleUrls: ['./logs-view.component.scss']
})
export class LogsViewComponent implements OnInit {
  @ViewChild('scrollMe') private myScrollContainer: ElementRef;

  @Input()
  public model: LogViewModel;

  @Input()
  public remove: Function;

  lockScroll: boolean = false;

  constructor() { }

  ngOnInit() { }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      if (!this.lockScroll) {
        this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.log('[Error]:' + err.toString());
    }
  }

  scrollToElement(position: number): void {
    let tracesList: NodeListOf<HTMLLIElement> = this.getElementsList();
    let offset: number = this.getElementOffsetTop(tracesList[position]);
    this.lockScroll = true;
    this.myScrollContainer.nativeElement.scrollTop = offset;
  }

  getElementsList(): any {
    return this.myScrollContainer.nativeElement.getElementsByTagName('li');
  }
  getElementOffsetTop(element: HTMLLIElement): number {
    let offset: number = element.offsetTop - element.parentElement.offsetTop;
    return offset;
  }

  lockLogScroll(switchLock: boolean): void {
    if (switchLock !== undefined) {
      this.lockScroll = switchLock;
    } else {
      this.lockScroll = !this.lockScroll;
    }
  }


  @HostListener('scroll', ['$event'])
  onScroll(event: Event | any): void {
    // Do scroll
    this.myScrollContainer.nativeElement.scrollTop = event.srcElement.scrollTop;
    this.lockLogScroll(!this.scrollIsInBottom(event));
  }

  scrollIsInBottom(event: Event | any): boolean {
    let pos: number = event.srcElement.scrollTop + event.srcElement.offsetHeight;
    let max: number = event.srcElement.scrollHeight;

    return pos === max;
  }

  scrollIsInTop(event: Event | any) {
    return event.srcElement.scrollTop === 0;
  }
}
