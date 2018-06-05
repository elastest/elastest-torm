import { Observable } from 'rxjs/Rx';
import { ElastestESService } from '../../services/elastest-es.service';
import { LogViewModel } from '../log-view-model';
import { ESRangeModel } from '../../elasticsearch-model/es-query-model';

export class ESRabLogModel implements LogViewModel {
  elastestESService: ElastestESService;

  name: string;
  traces: any[];
  filteredTraces: any[];
  prevTraces: any[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;
  etType: string;
  component: string;
  stream: string;
  monitoringIndex: string;
  maxsize: number = 1000;

  constructor(elastestESService: ElastestESService) {
    this.name = '';
    this.prevTraces = [];
    this.traces = [];
    this.filteredTraces = [];
    this.prevLoaded = false;
    this.hidePrevBtn = false;
    this.etType = '';
    this.component = '';
    this.stream = '';
    this.monitoringIndex = '';

    this.elastestESService = elastestESService;
  }

  getAllLogs(): void {
    this.getAllLogsSubscription().subscribe((data) => {
      this.traces = data;
    });
  }

  getAllLogsSubscription(timeRange?: ESRangeModel): Observable<any[]> {
    return this.elastestESService.searchAllLogs(this.monitoringIndex, this.stream, this.component, timeRange);
  }

  loadPrevious(): void {
    let tracesArrayToCompare: any[] = this.traces;
    if (!this.prevLoaded && this.prevTraces.length > 0) {
      tracesArrayToCompare = this.prevTraces;
    }
    this.elastestESService
      .getPrevLogsFromTrace(this.monitoringIndex, tracesArrayToCompare, this.stream, this.component)
      .subscribe(
        (data) => {
          if (data.length > 0) {
            this.prevTraces = data.concat(this.prevTraces);
          }

          this.prevLoaded = true; // If data.length > 0 already loaded all traces, else, there aren't traces to load
        },
        (error) => (this.prevLoaded = true), // 'There isn\'t reference traces yet to load previous'
      );
  }

  selectTimeRange(domain): void {
    this.filteredTraces = [];
    let counter: number = 0;
    for (let trace of [...this.traces]) {
      let time: Date = new Date(trace.timestamp);
      if (time >= domain[0] && time <= domain[1]) {
        this.filteredTraces.push(trace);
        counter++;
      }
    }

    if (counter === 0 && this.filteredTraces.length === 0) {
      this.filteredTraces = [];
      this.filteredTraces.push({ message: 'Nothing to show' });
    }
  }

  unselectTimeRange(): void {
    this.filteredTraces = [];
  }

  getTracePositionByTime(timeSelected): number {
    let position: number = 0;
    let found: boolean = false;
    let tracesList: any[] = this.filteredTraces.length > 0 ? this.filteredTraces : this.prevTraces.concat(this.traces);

    for (let trace of tracesList) {
      let time: Date = new Date(trace.timestamp);
      if (time < timeSelected && tracesList[position + 1] !== undefined) {
        let nextTraceTime: Date = new Date(tracesList[position + 1].timestamp);
        if (nextTraceTime >= timeSelected) {
          found = true;
          position++;
          break;
        }
      } else {
        if (time === timeSelected) {
          found = true;
        }
        break;
      }
      position++;
    }

    if (found) {
      return position;
    } else {
      return -1;
    }
  }

  clearFilter(): void {
    this.filteredTraces = [];
  }

  loadLastTraces(size: number = 10): void {
    this.elastestESService.getLastLogTraces(this.monitoringIndex, this.stream, this.component, size).subscribe((data) => {
      if (this.prevTraces.length === 0) {
        this.prevTraces = data.concat(this.prevTraces);
        // Keep prevLoaded to false
      }
    });
  }
}
