import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { of } from 'rxjs';
import {
  JournalSummaryResponse,
  JournalsService,
  PublicationStatus,
  PublicationsService,
} from '@core/api';
import { PublicationFilterState } from './publication-filter-state';
import { Publications } from './publications';

const EMPTY_PAGE = { content: [], totalElements: 0, totalPages: 0, page: 0, size: 20 };

function journal(id: string): JournalSummaryResponse {
  return { id, name: `Journal ${id}`, categories: [], isInterest: false };
}

describe('Publications', () => {
  let fixture: ComponentFixture<Publications>;
  let component: Publications;
  let state: PublicationFilterState;
  let publicationsService: {
    listMyPublications: ReturnType<typeof vi.fn>;
    deletePublication: ReturnType<typeof vi.fn>;
  };
  let journalsService: { searchJournals: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    publicationsService = {
      listMyPublications: vi.fn().mockReturnValue(of(EMPTY_PAGE)),
      deletePublication: vi.fn().mockReturnValue(of(undefined)),
    };
    journalsService = {
      searchJournals: vi.fn().mockReturnValue(of(EMPTY_PAGE)),
    };

    TestBed.configureTestingModule({
      imports: [Publications],
      providers: [
        PublicationFilterState,
        { provide: PublicationsService, useValue: publicationsService },
        { provide: JournalsService, useValue: journalsService },
        { provide: Router, useValue: { navigate: vi.fn() } },
        { provide: MessageService, useValue: { add: vi.fn() } },
        { provide: ConfirmationService, useValue: { confirm: vi.fn() } },
        { provide: TranslateService, useValue: { instant: (k: string) => k } },
      ],
    });

    fixture = TestBed.createComponent(Publications);
    component = fixture.componentInstance;
    state = TestBed.inject(PublicationFilterState);
  });

  it('loads the first page with no filters', () => {
    component.load();

    expect(publicationsService.listMyPublications).toHaveBeenCalledWith(
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      0,
      20,
    );
  });

  it('applies the status filter, resets to the first page and reloads', () => {
    component.first.set(40);

    component.onStatusChange(PublicationStatus.Planned);

    expect(component.first()).toBe(0);
    expect(publicationsService.listMyPublications).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      PublicationStatus.Planned,
      undefined,
      undefined,
      0,
      20,
    );
  });

  it('passes the selected journal id', () => {
    component.journal = journal('j1');

    component.onJournalSelect();

    expect(publicationsService.listMyPublications).toHaveBeenLastCalledWith(
      undefined,
      'j1',
      undefined,
      undefined,
      undefined,
      0,
      20,
    );
  });

  it('passes onlyAsMainAuthor when enabled', () => {
    component.onMainAuthorChange(true);

    expect(publicationsService.listMyPublications).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      undefined,
      undefined,
      true,
      0,
      20,
    );
  });

  it('clears all filters and reloads', () => {
    component.title = 'foo';
    component.journal = journal('j1');
    component.status = PublicationStatus.Submitted;
    component.minDaysInStatus = 30;
    component.onlyAsMainAuthor = true;

    component.clearFilters();

    expect(component.hasActiveFilters()).toBe(false);
    expect(publicationsService.listMyPublications).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      0,
      20,
    );
  });

  it('persists the active filters in the shared state', () => {
    component.title = 'deep';
    component.status = PublicationStatus.UnderReview;

    component.load();

    expect(state.filters.title).toBe('deep');
    expect(state.filters.status).toBe(PublicationStatus.UnderReview);
  });

  it('loads the requested page on page change', () => {
    component.onPageChange({ first: 20, rows: 20 });

    expect(publicationsService.listMyPublications).toHaveBeenLastCalledWith(
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      1,
      20,
    );
  });
});
