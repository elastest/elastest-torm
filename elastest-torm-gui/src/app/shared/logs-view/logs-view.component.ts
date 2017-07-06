import { ElasticSearchService } from '../services/elasticsearch.service';
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

  lockScroll: boolean = false;

  constructor(
        private elasticService: ElasticSearchService,
  ) { }

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


  loadPreviousLogs() {
      if (this.logView.traces[0] !== undefined && this.logView.traces[0] !== null) {
      this.elasticService.getFromGivenLog(this.logView.logUrl, this.logView.traces[0], this.logView.logType)
        .subscribe(
        (messages) => {
          this.logView.prevTraces = messages;
          this.logView.prevTracesLoaded = true;
        },
        (error) => console.log(error),
      );
    }
  }
}
