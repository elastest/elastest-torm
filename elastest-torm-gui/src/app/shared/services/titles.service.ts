import { BreadcrumbService } from '../breadcrumb/breadcrumb.service';
import { Meta, Title } from '@angular/platform-browser';
import { Injectable } from '@angular/core';
import { ProjectService } from '../../elastest-etm/project/project.service';
import { TJobService } from '../../elastest-etm/tjob/tjob.service';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { TJobExecService } from '../../elastest-etm/tjob-exec/tjobExec.service';
import { TestLinkService } from '../../etm-testlink/testlink.service';
import { TestProjectModel } from '../../etm-testlink/models/test-project-model';
import { TLTestSuiteModel } from '../../etm-testlink/models/test-suite-model';
import { TLTestCaseModel } from '../../etm-testlink/models/test-case-model';
import { TestPlanModel } from '../../etm-testlink/models/test-plan-model';
import { BuildModel } from '../../etm-testlink/models/build-model';
import { TestCaseExecutionModel } from '../../etm-testlink/models/test-case-execution-model';
import { TestCaseService } from '../../elastest-etm/test-case/test-case.service';
import { TestCaseModel } from '../../elastest-etm/test-case/test-case-model';

@Injectable()
export class TitlesService {
  constructor(
    private _titleService: Title,
    private meta: Meta,
    private breadcrumbService: BreadcrumbService,
    private projectService: ProjectService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private testLinkService: TestLinkService,
    private testCaseService: TestCaseService,
  ) {
    this.breadcrumbService.addFriendlyNameForRoute('/projects', 'Projects');
    this.breadcrumbService.addFriendlyNameForRoute('/projects/add', 'New Project');
    this.breadcrumbService.addFriendlyNameForRoute('/tjobexecs', 'Dashboard');
    this.breadcrumbService.addFriendlyNameForRoute('/test-engines', 'Test Engines');
    this.breadcrumbService.addFriendlyNameForRoute('/support-services', 'Test Support Services');
    this.breadcrumbService.addFriendlyNameForRoute('/loganalyzer', 'Log Analyzer');
    this.breadcrumbService.addFriendlyNameForRouteRegex('/loganalyzer?.*', '/ Log Analyzer');
    this.breadcrumbService.addFriendlyNameForRoute('^/testlink', 'Testlink Projects');
    this.breadcrumbService.addFriendlyNameForRoute('/help', 'About Elastest');
    this.breadcrumbService.addFriendlyNameForRouteRegex('(.*/sut/new)$', 'New SuT');
    this.breadcrumbService.addFriendlyNameForRouteRegex('(.*/tjob/new)$', 'New TJob');

    breadcrumbService.hideRouteRegex('.*edit$');
    breadcrumbService.hideRouteRegex('.*sut$');
    breadcrumbService.hideRouteRegex('.*tjob$');
    breadcrumbService.hideRouteRegex('.*tjob-exec$');
    breadcrumbService.hideRouteRegex('.*testlink/projects$');
    breadcrumbService.hideRouteRegex('.*suites$');
    breadcrumbService.hideRouteRegex('.*cases$');
    breadcrumbService.hideRouteRegex('.*plans$');
    breadcrumbService.hideRouteRegex('.*builds$');
    breadcrumbService.hideRouteRegex('.*/execs$');
    breadcrumbService.hideRouteRegex('external.*');
    breadcrumbService.hideRouteRegex('.*/execs$');
    breadcrumbService.hideRouteRegex('.*/testSuite$'); // TODO delete when testSuite was used.
    breadcrumbService.hideRouteRegex('.*/testSuite/(\\d+)$');
    breadcrumbService.hideRouteRegex('.*/testCase$');
  }

  setHeadTitle(title: string): void {
    // Tab Title
    this._titleService.setTitle(title);
  }

  setPathName(url: string, name?: string): void {
    let groups: string[] = this.getGroups(url);

    if (name) {
      this.breadcrumbService.addFriendlyNameForRoute(url, name);
    } else {
      for (let group of groups) {
        let groupArr: string[] = group.split('/');
        if (groupArr[0] === 'projects') {
          this.projectService.getProject(groupArr[1]).subscribe(
            (project: ProjectModel) => {
              this.breadcrumbService.addFriendlyNameForRouteRegex('.*/projects/' + groupArr[1] + '$', '/ ' + project.name);
            },
            (error) => console.log(error),
          );
        } else if (groupArr[0] === 'tjob') {
          this.tJobService.getTJob(groupArr[1]).subscribe(
            (tjob: TJobModel) => {
              this.breadcrumbService.addFriendlyNameForRouteRegex('.*/tjob/' + groupArr[1] + '$', '/ ' + tjob.name);
            },
            (error) => console.log(error),
          );
        } else if (groupArr[0] === 'tjob-exec') {
          this.breadcrumbService.addFriendlyNameForRouteRegex('.*/tjob-exec/' + groupArr[1] + '$', '/ Execution ' + groupArr[1]);
        } else if (groupArr[0] === 'testCase') {
          this.testCaseService.getTestCaseById(+groupArr[1]).subscribe((testCase: TestCaseModel) => {
            this.breadcrumbService.addFriendlyNameForRouteRegex(
              '.*/testCase/' + groupArr[1] + '$',
              '/ Test Case ' + testCase.name,
            );
          });
        } else if (groupArr[0] === 'testlink') {
          if (groupArr[1] && groupArr[1] === 'projects') {
            this.testLinkService.getProjectById(groupArr[2]).subscribe(
              (project: TestProjectModel) => {
                this.breadcrumbService.addFriendlyNameForRouteRegex(
                  '.*/testlink/projects/' + groupArr[2] + '$',
                  '/ ' + project.name,
                );
              },
              (error) => console.log(error),
            );
          }
        } else if (Number(groupArr[0]) && groupArr[1] === 'suites') {
          this.testLinkService.getTestSuiteById(groupArr[2], groupArr[0]).subscribe((tsuite: TLTestSuiteModel) => {
            this.breadcrumbService.addFriendlyNameForRouteRegex('.*suites/' + groupArr[2] + '$', '/ ' + tsuite.name);
          });
        } else if (groupArr[0] === 'cases') {
          this.testLinkService.getTestCaseById(groupArr[1]).subscribe(
            (testCase: TLTestCaseModel) => {
              this.breadcrumbService.addFriendlyNameForRouteRegex('.*cases/' + groupArr[1] + '$', '/ ' + testCase.name);
            },
            (error) => console.log(error),
          );
        } else if (groupArr[0] === 'plans') {
          this.testLinkService.getTestPlanById(groupArr[1]).subscribe(
            (testPlan: TestPlanModel) => {
              this.breadcrumbService.addFriendlyNameForRouteRegex('.*plans/' + groupArr[1] + '$', '/ ' + testPlan.name);
            },
            (error) => console.log(error),
          );
        } else if (groupArr[0] === 'builds') {
          this.testLinkService.getBuildById(groupArr[1]).subscribe(
            (build: BuildModel) => {
              this.breadcrumbService.addFriendlyNameForRouteRegex('.*builds/' + groupArr[1] + '$', '/ ' + build.name);
            },
            (error) => console.log(error),
          );
        } else if (Number(groupArr[0]) && groupArr[1] === 'execs') {
          this.testLinkService.getTestExecById(groupArr[0], groupArr[2]).subscribe((exec: TestCaseExecutionModel) => {
            this.breadcrumbService.addFriendlyNameForRouteRegex('.*execs/' + groupArr[2] + '$', '/ Execution ' + exec.id);
          });
        }
      }
    }
  }

  getTitle(): Title {
    return this._titleService;
  }

  private getGroups(url: string): string[] {
    let arr: string[] = url.split('/');
    arr.shift();
    let matches: string[] = [];
    for (let i: number = 0; i < arr.length; i++) {
      if (arr[i + 1] && Number(arr[i + 1])) {
        matches.push(arr[i] + '/' + arr[i + 1]);
      }
      // It need the previous id to get suites and execs
      if ((arr[i] === 'suites' || arr[i] === 'execs') && arr[i - 1]) {
        matches[matches.length - 1] = arr[i - 1] + '/' + matches[matches.length - 1];
      }
    }
    if (arr[0] && arr[0] === 'testlink') {
      matches[0] = 'testlink/' + matches[0];
    }
    return matches;
  }
}
