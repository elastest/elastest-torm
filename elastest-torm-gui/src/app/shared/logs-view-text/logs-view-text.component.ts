import { Component, OnInit, Input, ViewChild, HostListener, ElementRef } from '@angular/core';
import { LogViewModel } from '../logs-view/log-view-model';

@Component({
  selector: 'logs-view-text',
  templateUrl: './logs-view-text.component.html',
  styleUrls: ['./logs-view-text.component.scss'],
})
export class LogsViewTextComponent implements OnInit {
  @ViewChild('scrollMe')
  private myScrollContainer: ElementRef;
  @Input()
  public model: LogViewModel;

  public lockScroll: boolean = false;

  constructor() {}

  ngOnInit() {}

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
    this.lockScroll = true;
    let tracesList: NodeListOf<HTMLLIElement> = this.getElementsList();
    let offset: number = this.getElementOffsetTop(tracesList[position]);
    this.myScrollContainer.nativeElement.scrollTop = offset;
  }

  getElementsList(): any {
    return this.myScrollContainer.nativeElement.getElementsByTagName('li');
  }
  getElementOffsetTop(element: HTMLLIElement): number {
    let offset: number = element.offsetTop - element.parentElement.offsetTop;
    return offset;
  }

  @HostListener('scroll', ['$event'])
  onScroll(event: Event | any): void {
    // Do scroll
    this.myScrollContainer.nativeElement.scrollTop = event.srcElement.scrollTop;
    this.lockLogScroll(!this.scrollIsInBottom(event));
  }

  scrollIsInBottom(event: Event | any): boolean {
    let errorMarginOfDifference: number = 2;
    let pos: number = event.srcElement.scrollTop + event.srcElement.offsetHeight;
    let max: number = event.srcElement.scrollHeight;

    return pos === max || max - pos < errorMarginOfDifference;
  }

  scrollIsInTop(event: Event | any) {
    return event.srcElement.scrollTop === 0;
  }

  lockLogScroll(switchLock: boolean): void {
    if (switchLock !== undefined) {
      this.lockScroll = switchLock;
    } else {
      this.lockScroll = !this.lockScroll;
    }
  }
}
