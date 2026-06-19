import { Component, viewChildren } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primeng/tabs';
import { JournalList } from './journal-list';

@Component({
  selector: 'record-journals',
  imports: [TranslatePipe, Tabs, TabList, Tab, TabPanels, TabPanel, JournalList],
  templateUrl: './journals.html',
})
export class Journals {
  private readonly lists = viewChildren(JournalList);

  /**
   * Both tab panels stay alive (PrimeNG only hides the inactive one), so their data can go stale
   * after toggling an interest in the other tab. Reload the tab being activated to keep them in sync.
   */
  onTabChange(value: string | number | undefined) {
    this.lists()
      .find((list) => list.mode() === value)
      ?.reload();
  }
}
