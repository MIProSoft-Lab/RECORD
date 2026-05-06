import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'record-dashboard',
  imports: [TranslatePipe],
  templateUrl: './dashboard.html',
})
export class Dashboard {}
