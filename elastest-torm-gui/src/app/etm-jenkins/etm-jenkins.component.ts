import { Component, OnInit, Input, OnDestroy, HostListener } from '@angular/core';
import { TitlesService } from '../shared/services/titles.service';
import { TestEnginesService } from '../elastest-test-engines/test-engines.service';
import { EtPluginModel } from '../elastest-test-engines/et-plugin-model';
import { Subscription, Observable } from 'rxjs';

@Component({
  selector: 'etm-jenkins',
  templateUrl: './etm-jenkins.component.html',
  styleUrls: ['./etm-jenkins.component.scss'],
})
export class EtmJenkinsComponent implements OnInit, OnDestroy {
  @Input()
  isNested: boolean = false;
  jenkinsUrl: string;
  jenkinsModel: EtPluginModel;
  isStarted: boolean = false;
  startingInProcess: boolean = true;

  timer: Observable<number>;
  subscription: Subscription;

  constructor(private titlesService: TitlesService, private testEnginesService: TestEnginesService) {}

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('Jenkins');
    }

    this.testEnginesService.getUniqueEtPlugin('jenkins').subscribe(
      (jenkinsModel: EtPluginModel) => {
        this.jenkinsModel = jenkinsModel;
        this.initIfStarted();
        if (jenkinsModel.isNotInitialized()) {
          this.startingInProcess = false;
        } else {
          this.waitForReady();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.unsubscribe();
  }

  unsubscribe(): void {
    if (this.subscription !== undefined) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  waitForReady(): void {
    this.timer = Observable.interval(1800);
    if (
      this.jenkinsModel.isCreated() &&
      !this.jenkinsModel.isReady() &&
      (this.subscription === null || this.subscription === undefined)
    ) {
      this.subscription = this.timer.subscribe(() => {
        this.testEnginesService.getEtPlugin(this.jenkinsModel.name).subscribe((etPlugin: EtPluginModel) => {
          this.jenkinsModel = etPlugin;
          if (etPlugin.isReady()) {
            this.initIfStarted();
            this.subscription.unsubscribe();
            this.subscription = undefined;
          }
        });
      });
    }
  }

  initIfStarted(): void {
    this.testEnginesService.isStarted(this.jenkinsModel).subscribe(
      (started: boolean) => {
        this.isStarted = started;
        if (started) {
          this.startingInProcess = false;
          this.loadJenkinsUrl();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  startJenkins(): void {
    this.startingInProcess = true;
    this.testEnginesService.startEtPluginAsync(this.jenkinsModel).subscribe(
      (jenkinsModel: EtPluginModel) => {
        this.jenkinsModel = jenkinsModel;
        this.waitForReady();
      },
      (error: Error) => {
        console.log(error);
        this.startingInProcess = false;
      },
    );
  }

  loadJenkinsUrl(): void {
    this.testEnginesService.getUrl(this.jenkinsModel.name).subscribe((url: string) => {
      this.jenkinsUrl = url;
    });
  }
}
