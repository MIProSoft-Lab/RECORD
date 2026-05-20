import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { PendingInvitations } from '../invitations/pending-invitations';

@Component({
  selector: 'record-dashboard',
  imports: [TranslatePipe, PendingInvitations],
  templateUrl: './dashboard.html',
})
export class Dashboard {}
