import { Component, OnInit, Input } from '@angular/core';
import { TitlesService } from '../shared/services/titles.service';
import { TestEnginesService } from '../elastest-test-engines/test-engines.service';
import { EtPluginModel } from '../elastest-test-engines/et-plugin-model';

@Component({
  selector: 'etm-jenkins',
  templateUrl: './etm-jenkins.component.html',
  styleUrls: ['./etm-jenkins.component.scss'],
})
export class EtmJenkinsComponent implements OnInit {
  @Input()
  isNested: boolean = false;
  jenkinsUrl: string;
  jenkinsModel: EtPluginModel;
  isStarted: boolean = false;
  startingInProcess: boolean = true;

  constructor(private titlesService: TitlesService, private testEnginesService: TestEnginesService) {}

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('Jenkins');
    }

    this.testEnginesService.getUniqueEtPlugin('jenkins').subscribe(
      (jenkinsModel: EtPluginModel) => {
        this.jenkinsModel = jenkinsModel;
        this.startingInProcess = false;
        this.initIfStarted();
      },
      (error: Error) => console.log(error),
    );
  }

  initIfStarted(): void {
    this.testEnginesService.isStarted(this.jenkinsModel).subscribe(
      (started: boolean) => {
        this.isStarted = started;
        if (started) {
          this.init();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  startJenkins(): void {
    this.startingInProcess = true;
    this.testEnginesService.startEtPlugin(this.jenkinsModel).subscribe(
      (jenkinsModel: EtPluginModel) => {
        this.jenkinsModel = jenkinsModel;
        this.initIfStarted();
        this.startingInProcess = false;
      },
      (error: Error) => {
        console.log(error);
        this.startingInProcess = false;
      },
    );
  }

  init(): void {
    this.loadJenkinsUrl();
  }

  loadJenkinsUrl(): void {
    this.testEnginesService.getUrl(this.jenkinsModel.name).subscribe((url: string) => {
      this.jenkinsUrl = url;
    });
  }
}
