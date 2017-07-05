import { LogViewModel } from './log-view-model';

import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'logs-view',
  templateUrl: './logs-view.component.html',
  styleUrls: ['./logs-view.component.scss']
})
export class LogsViewComponent implements OnInit {
  @ViewChild('scrollMe') private myScrollContainer: ElementRef;

  @Input() 
  public logView: LogViewModel;
  @Input() 
  public loadPreviousLogs: Function;

  constructor() {

  }

  ngOnInit() {
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.log('[Error]:' + err.toString());
    }
  }
}
