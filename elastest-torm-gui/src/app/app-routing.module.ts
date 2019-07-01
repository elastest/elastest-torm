import { LiveTjobExecManagerComponent } from './elastest-etm/tjob-exec/live-tjob-exec-manager/live-tjob-exec-manager.component';
import { TLTestCaseComponent } from './etm-testlink/test-case/test-case.component';
import { TestCaseFormComponent } from './etm-testlink/test-case/test-case-form/test-case-form.component';
import { TLTestSuiteComponent } from './etm-testlink/test-suite/test-suite.component';
import { TestSuiteFormComponent } from './etm-testlink/test-suite/test-suite-form/test-suite-form.component';
import { TestPlanComponent } from './etm-testlink/test-plan/test-plan.component';
import { HelpComponent } from './elastest-etm/help/help.component';
import { ElastestLogAnalyzerComponent } from './elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecsManagerComponent } from './elastest-etm/tjob-exec/tjob-execs-manager/tjob-execs-manager.component';
import { ProjectManagerComponent } from './elastest-etm/project/project-manager/project-manager.component';
import { TestEngineViewComponent } from './elastest-test-engines/test-engine-view/test-engine-view.component';
import { ElastestTestEnginesComponent } from './elastest-test-engines/elastest-test-engines.component';
import { ServiceDetailComponent } from './elastest-esm/support-services/service-detail/service-detail.component';
import { ServiceGuiComponent } from './elastest-esm/support-services/service-gui/service-gui.component';
import { InstancesManagerComponent } from './elastest-esm/support-services/instance-manager/instances-manager.component';
import { TestVncComponent } from './shared/vnc-client/test-vnc/test-vnc.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { EtmComponent } from './elastest-etm/etm.component';
import { ProjectFormComponent } from './elastest-etm/project/project-form/project-form.component';
import { ProjectsManagerComponent } from './elastest-etm/project/projects-manager/projects-manager.component';
import { SutsManagerComponent } from './elastest-etm/sut/suts-manager/suts-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { TJobFormComponent } from './elastest-etm/tjob/tjob-form/tjob-form.component';
import { SutFormComponent } from './elastest-etm/sut/sut-form/sut-form.component';
import { TJobsManagerComponent } from './elastest-etm/tjob/tjobs-manager/tjobs-manager.component';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { TjobExecManagerComponent } from './elastest-etm/tjob-exec/tjob-exec-manager/tjob-exec-manager.component';
import { ElastestEusComponent } from './elastest-eus/elastest-eus.component';
import { LoginComponent } from './login/login.component';
import { RedirectComponent } from './shared/redirect/redirect.component';
import { EtmTestlinkComponent } from './etm-testlink/etm-testlink.component';
import { TestProjectFormComponent } from './etm-testlink/test-project/test-project-form/test-project-form.component';
import { TestProjectComponent } from './etm-testlink/test-project/test-project.component';
import { TestPlanFormComponent } from './etm-testlink/test-plan/test-plan-form/test-plan-form.component';
import { TestCaseStepComponent } from './etm-testlink/test-case-step/test-case-step.component';
import { TestCaseStepFormComponent } from './etm-testlink/test-case-step/test-case-step-form/test-case-step-form.component';
import { BuildComponent } from './etm-testlink/build/build.component';
import { TestCaseExecsComponent } from './etm-testlink/build/test-case-execs/test-case-execs.component';
import { ExecutionComponent } from './etm-testlink/execution/execution.component';
import { ExternalTestExecutionFormComponent } from './elastest-etm/external/external-test-execution/external-test-execution-form/external-test-execution-form.component';
import { ExternalTjobComponent } from './elastest-etm/external/external-tjob/external-tjob.component';
import { ExternalTjobExecutionComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution.component';
import { ExternalTjobExecutionNewComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution-new/external-tjob-execution-new.component';
import { ExternalTjobFormComponent } from './elastest-etm/external/external-tjob/external-tjob-form/external-tjob-form.component';
import { ExternalTestExecutionComponent } from './elastest-etm/external/external-test-execution/external-test-execution.component';
import { EdmContainerComponent } from './elastest-edm/edm-container/edm-container.component';
import { EmpContainerComponent } from './elastest-emp/emp-container/emp-container.component';
import { TestPlanExecutionComponent } from './etm-testlink/test-plan/test-plan-execution/test-plan-execution.component';
import { TestSuiteComponent } from './elastest-etm/test-suite/test-suite.component';
import { TestCaseComponent } from './elastest-etm/test-case/test-case.component';
import { EtmJenkinsComponent } from './etm-jenkins/etm-jenkins.component';
import { DashboardComponent } from './elastest-etm/dashboard/dashboard.component';
import { TjobExecViewComponent } from './elastest-etm/tjob-exec/tjob-exec-view/tjob-exec-view.component';
import { TjobExecsComparatorComponent } from './elastest-etm/tjob-exec/tjob-execs-comparator/tjob-execs-comparator.component';
import { ManageElastestComponent } from './elastest-etm/manage-elastest/manage-elastest.component';
import { CrossbrowserComponentComponent } from './elastest-eus/crossbrowser-component/crossbrowser-component.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: '',
    component: EtmComponent,
    children: [
      {
        component: DashboardComponent,
        path: '',
      },
      {
        path: 'projects',
        children: [
          {
            path: '',
            component: ProjectsManagerComponent,
          },
          {
            path: 'add',
            component: ProjectFormComponent,
          },
          {
            path: 'edit/:projectId',
            component: ProjectFormComponent,
          },
          {
            path: 'edit',
            component: ProjectFormComponent,
          },
          {
            path: ':projectId',
            children: [
              {
                path: '',
                component: ProjectManagerComponent,
              },
              {
                path: 'tjob',
                children: [
                  {
                    path: 'edit/:tJobId',
                    component: TJobFormComponent,
                  },
                  {
                    path: 'new',
                    component: TJobFormComponent,
                  },
                  {
                    path: ':tJobId',
                    children: [
                      {
                        path: '',
                        component: TjobManagerComponent,
                      },
                      {
                        path: 'comparator',
                        component: TjobExecsComparatorComponent,
                      },
                      {
                        path: 'tjob-exec',
                        children: [
                          {
                            path: ':tJobExecId',
                            children: [
                              {
                                path: '',
                                component: TjobExecViewComponent,
                              },
                              {
                                path: 'loganalyzer',
                                component: ElastestLogAnalyzerComponent,
                              },
                              {
                                path: 'testSuite',
                                children: [
                                  {
                                    path: ':testSuiteId',
                                    children: [
                                      {
                                        path: '',
                                        component: TestSuiteComponent,
                                      },
                                      {
                                        path: 'testCase',
                                        children: [
                                          {
                                            path: ':testCaseId',
                                            children: [
                                              {
                                                path: '',
                                                component: TestCaseComponent,
                                              },
                                              {
                                                path: 'loganalyzer',
                                                component: ElastestLogAnalyzerComponent,
                                              },
                                            ],
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
              {
                path: 'sut',
                children: [
                  {
                    path: '',
                    component: SutManagerComponent,
                  },
                  {
                    path: 'edit/:sutId',
                    component: SutFormComponent,
                  },
                  {
                    path: 'new',
                    component: SutFormComponent,
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        path: 'tjobs',
        children: [
          {
            path: '',
            component: TJobsManagerComponent,
          },
        ],
      },
      {
        path: 'tjobexecs',
        children: [
          {
            path: '',
            component: TJobExecsManagerComponent,
          },
        ],
      },
      {
        path: 'suts',
        children: [
          {
            path: '',
            component: SutsManagerComponent,
          },
          {
            path: 'edit/:id',
            component: SutFormComponent,
          },
          {
            path: 'edit',
            component: SutFormComponent,
          },
        ],
      },
      {
        path: 'etm-app',
        component: EtmComponent,
      },
      {
        path: 'test-engines',
        children: [
          {
            path: '',
            component: ElastestTestEnginesComponent,
          },
          {
            path: ':name',
            component: TestEngineViewComponent,
          },
        ],
      },
      {
        path: 'support-services',
        children: [
          {
            path: '',
            component: InstancesManagerComponent,
          },
          {
            path: 'service-detail/:id',
            component: ServiceDetailComponent,
          },
          {
            path: 'service-gui',
            component: ServiceGuiComponent,
          },
        ],
      },
      {
        path: 'service-gui',
        component: ServiceGuiComponent,
      },
      {
        path: 'loganalyzer',
        component: ElastestLogAnalyzerComponent,
      },
      {
        path: 'vnc',
        component: TestVncComponent,
      },
      {
        path: 'eus',
        children: [
          {
            path: '',
            component: ElastestEusComponent,
          },
          {
            path: 'crossbrowser',
            children: [
              {
                path: ':crossbrowserId',
                component: CrossbrowserComponentComponent,
              },
            ],
          },
        ],
      },
      {
        path: 'redirect',
        component: RedirectComponent,
      },
      {
        path: 'help',
        component: HelpComponent,
      },
      {
        path: 'manage',
        component: ManageElastestComponent,
      },
      {
        path: 'jenkins',
        children: [
          {
            component: EtmJenkinsComponent,
            path: '',
          },
        ],
      },
      {
        path: 'testlink',
        children: [
          {
            component: EtmTestlinkComponent,
            path: '',
          },
          {
            path: 'projects',
            children: [
              {
                path: 'new',
                component: TestProjectFormComponent,
              },
              {
                path: 'edit/:projectId',
                component: TestProjectFormComponent,
              },
              {
                path: 'edit',
                component: TestProjectFormComponent,
              },
              {
                path: ':projectId',
                children: [
                  {
                    path: '',
                    component: TestProjectComponent,
                  },
                  {
                    path: 'plans',
                    children: [
                      {
                        path: 'edit/:planId',
                        component: TestPlanFormComponent,
                      },
                      {
                        path: 'new',
                        component: TestPlanFormComponent,
                      },
                      {
                        path: ':planId',
                        children: [
                          {
                            path: '',
                            component: TestPlanComponent,
                          },
                          {
                            path: 'builds',
                            children: [
                              {
                                path: 'edit/:buildId',
                                component: TestPlanFormComponent,
                              },
                              {
                                path: 'new',
                                component: TestPlanFormComponent,
                              },
                              {
                                path: ':buildId',
                                children: [
                                  {
                                    path: '',
                                    component: BuildComponent,
                                  },
                                  {
                                    path: 'exec',
                                    children: [
                                      {
                                        path: 'new',
                                        component: TestPlanExecutionComponent,
                                      },
                                    ],
                                  },
                                  {
                                    path: 'cases',
                                    children: [
                                      {
                                        path: ':caseId',
                                        children: [
                                          {
                                            path: '',
                                            component: TestCaseExecsComponent,
                                          },
                                          {
                                            path: 'execs',
                                            children: [
                                              {
                                                path: ':execId',
                                                children: [
                                                  {
                                                    path: '',
                                                    component: ExecutionComponent,
                                                  },
                                                ],
                                              },
                                            ],
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                  {
                    path: 'suites',
                    children: [
                      {
                        path: 'edit/:suiteId',
                        component: TestSuiteFormComponent,
                      },
                      {
                        path: 'new',
                        component: TestSuiteFormComponent,
                      },
                      {
                        path: ':suiteId',
                        children: [
                          {
                            path: '',
                            component: TLTestSuiteComponent,
                          },
                          {
                            path: 'cases',
                            children: [
                              {
                                path: 'edit/:caseId',
                                component: TestCaseFormComponent,
                              },
                              {
                                path: 'new',
                                component: TestCaseFormComponent,
                              },
                              {
                                path: ':caseId',
                                children: [
                                  {
                                    path: '',
                                    children: [
                                      {
                                        path: '',
                                        component: TLTestCaseComponent,
                                      },
                                      {
                                        path: 'steps',
                                        children: [
                                          {
                                            path: 'edit/:stepId',
                                            component: TestCaseStepFormComponent,
                                          },
                                          {
                                            path: 'new',
                                            component: TestCaseStepFormComponent,
                                          },
                                          {
                                            path: ':stepId',
                                            children: [
                                              {
                                                path: '',
                                                component: TestCaseStepComponent,
                                              },
                                            ],
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        path: 'external',
        children: [
          {
            path: 'execute',
            component: ExternalTestExecutionFormComponent,
          },
          // {
          //     path: 'new',
          //     component: TestProjectFormComponent,
          // },
          // {
          //     path: 'edit/:projectId',
          //     component: TestProjectFormComponent,
          // },

          {
            path: 'projects',
            children: [
              // {
              //     path: '',
              //     component: TestProjectComponent,
              // },
              {
                path: ':exProjectId',
                children: [
                  // {
                  //     path: '',
                  //     component: TestProjectComponent,
                  // },
                  {
                    path: 'tjob',
                    children: [
                      {
                        path: 'edit/:tJobId',
                        component: ExternalTjobFormComponent,
                      },
                      {
                        path: ':tJobId',
                        children: [
                          {
                            path: '',
                            component: ExternalTjobComponent,
                          },
                          {
                            path: 'exec',
                            children: [
                              {
                                path: 'new',
                                component: ExternalTjobExecutionNewComponent,
                              },
                              {
                                path: ':execId',
                                children: [
                                  {
                                    path: '',
                                    component: ExternalTjobExecutionComponent,
                                  },
                                ],
                              },
                            ],
                          },
                          {
                            path: 'case',
                            children: [
                              {
                                path: ':caseId',
                                children: [
                                  {
                                    path: 'exec',
                                    children: [
                                      {
                                        path: ':execId',
                                        children: [
                                          {
                                            path: '',
                                            component: ExternalTestExecutionComponent,
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                  {
                    path: 'sut',
                    children: [
                      {
                        path: '',
                        component: SutManagerComponent,
                      },
                      {
                        path: 'edit/:sutId',
                        component: SutFormComponent,
                      },
                      {
                        path: 'new',
                        component: SutFormComponent,
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        path: 'edm',
        children: [
          {
            path: '',
            component: EdmContainerComponent,
          },
        ],
      },
      {
        path: 'emp',
        children: [
          {
            path: '',
            component: EmpContainerComponent,
          },
        ],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
export const routedComponents: any[] = [
  LoginComponent,
  TJobsManagerComponent,
  ProjectsManagerComponent,
  SutManagerComponent,
  SutsManagerComponent,
  EtmComponent,
  LiveTjobExecManagerComponent,
  ProjectFormComponent,
  TjobManagerComponent,
  TJobFormComponent,
  TjobExecManagerComponent,
  TjobExecViewComponent,
  SutFormComponent,
  ElastestEusComponent,
  InstancesManagerComponent,
  ServiceGuiComponent,
  ServiceDetailComponent,
  HelpComponent,
  ManageElastestComponent,
];

export const appRoutes: any = RouterModule.forRoot(routes, { useHash: true });
