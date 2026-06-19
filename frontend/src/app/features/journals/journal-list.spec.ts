import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { JournalSummaryResponse, JournalsService } from '@core/api';
import { JournalList } from './journal-list';
import { JournalSearchState } from './journal-search-state';

function journal(id: string, isInterest: boolean): JournalSummaryResponse {
  return { id, name: `Journal ${id}`, categories: [], isInterest };
}

describe('JournalList', () => {
  let fixture: ComponentFixture<JournalList>;
  let component: JournalList;
  let ref: ComponentRef<JournalList>;
  let journalsService: {
    listJournalCategories: ReturnType<typeof vi.fn>;
    searchJournals: ReturnType<typeof vi.fn>;
    listInterestJournals: ReturnType<typeof vi.fn>;
    markJournalAsInterest: ReturnType<typeof vi.fn>;
    unmarkJournalAsInterest: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    journalsService = {
      listJournalCategories: vi.fn().mockReturnValue(of([])),
      searchJournals: vi.fn().mockReturnValue(of({ content: [], totalElements: 0 })),
      listInterestJournals: vi.fn().mockReturnValue(of({ content: [], totalElements: 0 })),
      markJournalAsInterest: vi.fn().mockReturnValue(of(undefined)),
      unmarkJournalAsInterest: vi.fn().mockReturnValue(of(undefined)),
    };

    TestBed.configureTestingModule({
      imports: [JournalList],
      providers: [
        JournalSearchState,
        { provide: JournalsService, useValue: journalsService },
        { provide: Router, useValue: { navigate: vi.fn() } },
        { provide: MessageService, useValue: { add: vi.fn() } },
        { provide: TranslateService, useValue: { instant: (k: string) => k } },
      ],
    });

    fixture = TestBed.createComponent(JournalList);
    component = fixture.componentInstance;
    ref = fixture.componentRef;
  });

  it('marks a journal as interest and stops row navigation', () => {
    ref.setInput('mode', 'search');
    const row = journal('1', false);
    component.journals.set([row]);
    const event = { stopPropagation: vi.fn() } as unknown as Event;

    component.toggleInterest(row, event);

    expect(event.stopPropagation).toHaveBeenCalled();
    expect(journalsService.markJournalAsInterest).toHaveBeenCalledWith('1');
    expect(component.journals()[0].isInterest).toBe(true);
  });

  it('reverts the optimistic mark when the request fails', () => {
    ref.setInput('mode', 'search');
    journalsService.markJournalAsInterest.mockReturnValue(throwError(() => new Error('boom')));
    const row = journal('1', false);
    component.journals.set([row]);

    component.toggleInterest(row, { stopPropagation: vi.fn() } as unknown as Event);

    expect(component.journals()[0].isInterest).toBe(false);
  });

  it('removes the row optimistically when unmarking in interests mode', () => {
    ref.setInput('mode', 'interests');
    const row = journal('1', true);
    component.journals.set([row, journal('2', true)]);
    component.totalRecords.set(2);

    component.toggleInterest(row, { stopPropagation: vi.fn() } as unknown as Event);

    expect(journalsService.unmarkJournalAsInterest).toHaveBeenCalledWith('1');
    expect(component.journals().map((j) => j.id)).toEqual(['2']);
    expect(component.totalRecords()).toBe(1);
  });

  it('reloads from the server when reactivated (keeping filters)', () => {
    ref.setInput('mode', 'interests');
    journalsService.listInterestJournals.mockReturnValue(
      of({ content: [journal('9', true)], totalElements: 1 }),
    );

    component.reload();

    expect(journalsService.listInterestJournals).toHaveBeenCalled();
    expect(component.journals().map((j) => j.id)).toEqual(['9']);
  });

  it('keeps the journal but flips the star when unmarking in search mode', () => {
    ref.setInput('mode', 'search');
    const row = journal('1', true);
    component.journals.set([row]);

    component.toggleInterest(row, { stopPropagation: vi.fn() } as unknown as Event);

    expect(journalsService.unmarkJournalAsInterest).toHaveBeenCalledWith('1');
    expect(component.journals()).toHaveLength(1);
    expect(component.journals()[0].isInterest).toBe(false);
  });
});
