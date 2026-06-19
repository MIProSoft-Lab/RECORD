import { Routes } from '@angular/router';
import { Login } from '@features/auth/pages/login/login';
import { MainLayout } from '@layouts/main-layout/main-layout';
import { Dashboard } from '@features/dashboard/dashboard';
import { Groups } from '@features/groups/groups';
import { GroupDetail } from '@features/groups/detail/group-detail';
import { Journals } from '@features/journals/journals';
import { JournalDetail } from '@features/journals/detail/journal-detail';
import { guestGuard } from '@core/guards/guest.guard';
import { authGuard } from '@core/guards/auth.guard';
import { Register } from '@features/auth/pages/register/register';
import { Settings } from '@features/settings/settings';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: Register,
    canActivate: [guestGuard]
  },
  {
    path: 'settings',
    component: Settings,
    canActivate: [authGuard]
  },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        component: Dashboard,
        data: { breadcrumb: 'DASHBOARD.TITLE' }
      },
      {
        path: 'groups',
        data: { breadcrumb: 'GROUPS.TITLE' },
        children: [
          { path: '', component: Groups },
          {
            path: ':id',
            component: GroupDetail,
            data: { breadcrumb: 'GROUPS.TITLE' }
          }
        ]
      },
      {
        path: 'journals',
        data: { breadcrumb: 'JOURNALS.TITLE' },
        children: [
          { path: '', component: Journals },
          {
            path: ':id',
            component: JournalDetail,
            data: { breadcrumb: 'JOURNALS.TITLE' }
          }
        ]
      },
      {
        path: '',
        redirectTo: '/dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
