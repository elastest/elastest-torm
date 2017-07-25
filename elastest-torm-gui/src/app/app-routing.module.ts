import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UsersComponent } from './users/users.component';
import { DashboardComponent } from './elastest-etm/dashboard/dashboard.component';
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
import { TOJobManagerComponent } from './elastest-etm/tojob/tojob-manager/tojob-manager.component';
import { ElastestEusComponent } from './elastest-eus/elastest-eus.component';
import { LoginComponent } from './login/login.component';
import { DashboardTemplateComponent } from './templates/dashboard/dashboard.component';
import { EditorTemplateComponent } from './templates/editor/editor.component';
import { EmailTemplateComponent } from './templates/email/email.component';
import { TemplatesComponent } from './templates/templates.component';
import { UsersFormComponent } from './users/form/form.component';
import { ElastestLogManagerComponent } from './elastest-log-manager/elastest-log-manager.component';

const routes: Routes = [
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: '',
        component: EtmComponent,
        children: [
            {
                component: ProjectsManagerComponent,
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
                                                path: 'tjob-exec',
                                                children: [
                                                    {
                                                        path: ':tJobExecId',
                                                        children: [
                                                            {
                                                                path: '',
                                                                component: TjobExecManagerComponent,
                                                            },
                                                            {
                                                                path: 'dashboard',
                                                                component: DashboardComponent,
                                                            }
                                                        ]
                                                    },
                                                ]
                                            },
                                        ]
                                    },
                                ]
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
                                ]
                            },
                        ]
                    },

                ]
            },
            {
                path: 'tjobs',
                children: [
                    {
                        path: '',
                        component: TJobsManagerComponent,
                    },
                    {
                        path: 'edit/:id',
                        component: TJobFormComponent,
                    },
                    {
                        path: 'edit',
                        component: TJobFormComponent,
                    },
                ]
            },
            {
                path: 'tojobs',
                component: TJobsManagerComponent,
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
                ]
            },
            {
                path: 'etm-app',
                component: EtmComponent,
            },
            {
                path: 'eus',
                component: ElastestEusComponent,
            },
            {
                path: 'logmanager',
                component: ElastestLogManagerComponent,
            },

        ]
    },
];

@NgModule({    
    imports: [
        RouterModule.forRoot(routes, { useHash: true, enableTracing: true }),
    ],
    exports: [
        RouterModule,
    ],
})
export class AppRoutingModule { }
export const routedComponents: any[] = [
    LoginComponent,
    TemplatesComponent, EditorTemplateComponent, EmailTemplateComponent, DashboardTemplateComponent,
    UsersComponent, UsersFormComponent,
    TJobsManagerComponent, ProjectsManagerComponent,
    SutManagerComponent, SutsManagerComponent, EtmComponent, TOJobManagerComponent, DashboardComponent, ProjectFormComponent,
    TjobManagerComponent, TJobFormComponent, TjobExecManagerComponent, SutFormComponent, ElastestEusComponent, ElastestLogManagerComponent,
];
export const appRoutes: any = RouterModule.forRoot(routes, { useHash: true });
