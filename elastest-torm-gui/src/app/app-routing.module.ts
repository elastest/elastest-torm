import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UsersComponent } from './users/users.component';
import { DashboardComponent } from './elastest-etm/dashboard/dashboard.component';
import { EtmComponent } from './elastest-etm/etm.component';
import { ProjectFormComponent } from './elastest-etm/project/project-form/project-form.component';
import { ProjectsManagerComponent } from './elastest-etm/project/projects-manager/projects-manager.component';
import { SutsManagerComponent } from './elastest-etm/sut/suts-manager/suts-manager.component';
import { TjobFormComponent } from './elastest-etm/tjob/tjob-form/tjob-form.component';
import { TJobsManagerComponent } from './elastest-etm/tjob/tjobs-manager/tjobs-manager.component';
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
                component: DashboardComponent,
                path: '',
            },
            {
                path: 'tjobs-management',
                component: TJobsManagerComponent,
            },
            {
                path: 'tojobs-management',
                component: TJobsManagerComponent,
            },
            {
                path: 'projects-management',
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
                        path: 'edit/:id',
                        component: ProjectFormComponent,
                    },
                    {
                        path: 'edit',
                        component: ProjectFormComponent,
                    },
                ]
            },
            {
                path: 'sut-management',
                component: SutsManagerComponent,
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
        RouterModule.forRoot(routes, { useHash: true }),
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
    SutsManagerComponent, EtmComponent, TOJobManagerComponent, DashboardComponent, ProjectFormComponent, ElastestEusComponent, TjobFormComponent, ElastestLogManagerComponent
];
