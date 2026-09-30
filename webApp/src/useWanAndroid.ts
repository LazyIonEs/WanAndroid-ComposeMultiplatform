import { useEffect, useRef, useState } from 'react';
import { WanAndroidWebClient } from 'sharedLogic';
import type { AppState, FeedSection, FeedState, Section } from './types';

const emptyFeed = (): FeedState => ({
  articles: [], loading: false, refreshing: false, loadingMore: false,
  hasMore: true, loaded: false, loadedPage: null, error: null, failedToLoadMore: false,
});

const initialState = (): AppState => ({
  home: emptyFeed(), plaza: emptyFeed(), projects: emptyFeed(),
  categories: [], selectedProjectId: null,
  categoriesLoading: false, categoriesError: null,
});

type Client = InstanceType<typeof WanAndroidWebClient>;

/** React subscribes to Kotlin state; requests, caches and pagination stay in Kotlin. */
export function useWanAndroid(section: Section) {
  const [state, setState] = useState<AppState>(initialState);
  const [bridgeError, setBridgeError] = useState<string | null>(null);
  const client = useRef<Client | null>(null);
  const visited = useRef(new Set<FeedSection>());

  useEffect(() => {
    const instance = new WanAndroidWebClient();
    client.current = instance;
    const stop = instance.watch((json: string) => {
      try {
        setState(JSON.parse(json) as AppState);
        setBridgeError(null);
      } catch {
        setBridgeError('读取页面数据失败，请刷新页面后重试。');
      }
    });
    visited.current = new Set(['home']);
    instance.start();
    return () => {
      stop();
      instance.close();
      client.current = null;
    };
  }, []);

  useEffect(() => {
    if (section !== 'settings' && !visited.current.has(section)) {
      visited.current.add(section);
      client.current?.refresh(section);
    }
  }, [section]);

  return {
    state,
    bridgeError,
    refresh: (feed: FeedSection) => client.current?.refresh(feed),
    loadMore: (feed: FeedSection) => client.current?.loadMore(feed),
    selectProject: (id: number) => client.current?.selectProject(id),
  };
}
