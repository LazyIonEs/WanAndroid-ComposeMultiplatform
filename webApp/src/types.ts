/** The JSON view state exported by the Kotlin sharedLogic module. */
export interface Article {
  id?: number | null;
  title: string;
  link: string;
  author?: string | null;
  shareUser?: string | null;
  niceDate?: string | null;
  niceShareDate?: string | null;
  chapterName?: string | null;
  desc?: string | null;
  envelopePic?: string | null;
  fresh?: boolean | null;
  pinned?: boolean;
}

export interface FeedState {
  articles: Article[];
  loading: boolean;
  refreshing: boolean;
  loadingMore: boolean;
  loaded: boolean;
  loadedPage: number | null;
  hasMore: boolean;
  error: string | null;
  failedToLoadMore: boolean;
}

export interface AppState {
  home: FeedState;
  plaza: FeedState;
  projects: FeedState;
  categories: { id: number; name: string }[];
  selectedProjectId: number | null;
  categoriesLoading: boolean;
  categoriesError: string | null;
}

export type FeedSection = 'home' | 'plaza' | 'projects';
export type Section = FeedSection | 'settings';
