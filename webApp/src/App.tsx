import { useEffect, useRef, useState, type CSSProperties, type ReactNode } from 'react';
import type { Article, FeedSection, FeedState, Section } from './types';
import { useWanAndroid } from './useWanAndroid';

const sections: Section[] = ['home', 'plaza', 'projects', 'settings'];
const labels: Record<Section, string> = { home: '首页', plaza: '广场', projects: '项目', settings: '设置' };

function readSection(): Section {
  const value = window.location.hash.slice(1);
  return sections.includes(value as Section) ? value as Section : 'home';
}

type IconName = Section | 'arrow' | 'sun' | 'moon' | 'system' | 'book' | 'offline';
function Icon({ name, className = '' }: { name: IconName; className?: string }) {
  const paths: Record<IconName, ReactNode> = {
    home: <><path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z" /></>,
    plaza: <><path d="M21 11a8 8 0 0 1-8 8H6l-4 3V11a8 8 0 0 1 8-8h3a8 8 0 0 1 8 8Z" /><path d="M7 10h9M7 14h6" /></>,
    projects: <><rect x="3" y="4" width="18" height="17" rx="3" /><path d="M8 2v4M16 2v4m-7 6-3 2 3 2m6-4 3 2-3 2" /></>,
    settings: <><path d="m9.2 3-.7 2-2.1.6-2-.4-2 3.4 1.4 1.6.1 2.2-1.4 1.6 2 3.4 2.1-.4 1.9 1.1.7 2h4l.7-2 1.9-1.1 2.1.4 2-3.4-1.4-1.6.1-2.2 1.4-1.6-2-3.4-2 .4-2.1-.6-.7-2Z" /><circle cx="11.2" cy="11.5" r="3" /></>,
    arrow: <><path d="M6 18 18 6M6 6h12v12" /></>,
    sun: <><circle cx="12" cy="12" r="4" /><path d="M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5m11 11L19 19M5 19l1.5-1.5m11-11L19 5" /></>,
    moon: <path d="M20.5 14A8.5 8.5 0 0 1 10 3.5 9 9 0 1 0 20.5 14Z" />,
    system: <><rect x="2" y="3" width="20" height="14" rx="2" /><path d="M8 21h8m-4-4v4" /></>,
    book: <><path d="M12 5v16m0-16C9 2 4 3 2 4v15c3-1 7-1 10 2 3-3 7-3 10-2V4c-2-1-7-2-10 1Z" /></>,
    offline: <><path d="m3 3 18 18M2 8a16 16 0 0 1 3-2m4-1a16 16 0 0 1 13 3M5 12a11 11 0 0 1 4-2m5 0a11 11 0 0 1 5 2m-11 4a6 6 0 0 1 7-1" /><circle cx="12" cy="20" r=".7" /></>,
  };
  return <svg className={`icon ${className}`} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>;
}

function safeWebUrl(value: string | null | undefined): string | undefined {
  if (!value?.trim()) return undefined;
  try {
    const url = new URL(value, 'https://www.wanandroid.com');
    return url.protocol === 'https:' || url.protocol === 'http:' ? url.href : undefined;
  } catch { return undefined; }
}

function ArticleCard({ article, project }: { article: Article; project: boolean }) {
  const author = article.author?.trim() || article.shareUser?.trim() || '社区分享';
  const date = article.niceDate?.trim() || article.niceShareDate?.trim();
  const link = safeWebUrl(article.link);
  const cover = safeWebUrl(article.envelopePic);
  const [imageFailed, setImageFailed] = useState(false);
  const hue = Array.from(author).reduce((total, char) => total + (char.codePointAt(0) ?? 0), 0) % 360;

  return (
    <li className={`article-card ${project ? 'project-card' : ''}`}>
      {project && cover && !imageFailed && <div className="project-cover"><img src={cover} alt="" loading="lazy" referrerPolicy="no-referrer" onError={() => setImageFailed(true)} /></div>}
      <article className="article-content">
        <div className="article-tags">
          {article.pinned && <span className="tag tag-accent">置顶</span>}
          {article.fresh && <span className="tag tag-accent">新</span>}
          <span className="tag">{article.chapterName || (project ? '开源项目' : '技术分享')}</span>
        </div>
        <h2>{link ? <a className="article-link" href={link} target="_blank" rel="noopener noreferrer">{article.title}<span className="sr-only">（在新标签页打开）</span></a> : article.title}</h2>
        {project && article.desc?.trim() && <p className="article-description">{article.desc}</p>}
        <div className="article-meta">
          <span className="author"><span className="avatar" style={{ '--avatar-hue': hue } as CSSProperties} aria-hidden="true">{Array.from(author)[0]}</span><span className="author-name">{author}</span></span>
          {date && <span className="article-date">{date}</span>}
          {link && <Icon name="arrow" className="article-arrow" />}
        </div>
      </article>
    </li>
  );
}

function ErrorNotice({ children, retry, label = '重新加载' }: { children: ReactNode; retry: () => void; label?: string }) {
  return <div className="error-notice" role="alert"><p>{children}</p><button className="text-button" onClick={retry}>{label}</button></div>;
}

function Placeholder({ kind, title, message, retry }: { kind: 'loading' | 'empty' | 'error'; title: string; message?: string; retry?: () => void }) {
  return <div className={`placeholder placeholder-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
    <div className="placeholder-icon" aria-hidden="true">{kind === 'loading' ? <span className="loader" /> : <Icon name={kind === 'error' ? 'offline' : 'book'} />}</div>
    <h2>{title}</h2>{message && <p>{message}</p>}
    {retry && <button className="primary-button" onClick={retry}>{kind === 'error' ? '重试' : '重新加载'}</button>}
  </div>;
}

function Feed({ feed, section, refresh, loadMore }: { feed: FeedState; section: FeedSection; refresh: () => void; loadMore: () => void }) {
  const busy = feed.loading || feed.refreshing;
  const empty = feed.articles.length === 0;
  const sentinel = useRef<HTMLDivElement>(null);
  const loadNext = useRef(loadMore);
  loadNext.current = loadMore;
  const supportsObserver = typeof IntersectionObserver !== 'undefined';

  useEffect(() => {
    const target = sentinel.current;
    if (!target || !supportsObserver || !feed.loaded || busy || feed.loadingMore || !feed.hasMore || feed.error || feed.failedToLoadMore) return;
    let requested = false;
    const observer = new IntersectionObserver((entries) => {
      if (!requested && entries.some((entry) => entry.isIntersecting)) {
        requested = true;
        observer.disconnect();
        loadNext.current();
      }
    }, { rootMargin: '0px 0px 240px 0px' });
    observer.observe(target);
    return () => observer.disconnect();
  }, [supportsObserver, empty, busy, feed.loaded, feed.loadedPage, feed.loadingMore, feed.hasMore, feed.error, feed.failedToLoadMore, feed.articles.length]);

  return (
    <section aria-label={`${labels[section]}文章`} aria-busy={busy || feed.loadingMore}>
      {!empty && feed.refreshing && <p className="feed-progress" role="status"><span className="loader" />正在更新内容…</p>}
      {!empty && feed.error && !feed.failedToLoadMore && <ErrorNotice retry={refresh}>{feed.error}</ErrorNotice>}
      {empty && (busy || feed.loadingMore || (!feed.error && (!feed.loaded || feed.hasMore))) ? (
        <Placeholder kind="loading" title={`正在加载${labels[section]}内容…`} message="精彩内容马上就来。" />
      ) : empty && feed.error ? (
        <Placeholder kind="error" title="内容加载失败" message={feed.error} retry={feed.failedToLoadMore ? loadMore : refresh} />
      ) : empty ? (
        <Placeholder kind="empty" title={section === 'projects' ? '这个分类暂时还没有项目' : '这里暂时还没有内容'} message="稍后再来看看，也许会有新的发现。" retry={refresh} />
      ) : !empty ? (
        <ul className={`article-grid ${section === 'projects' ? 'project-grid' : ''}`}>
          {feed.articles.map((article, index) => <ArticleCard key={`${article.id ?? article.link}-${index}`} article={article} project={section === 'projects'} />)}
        </ul>
      ) : null}
      <div ref={sentinel} className={empty ? 'load-sentinel' : 'pagination'} aria-live="polite">
        {!empty && (feed.loadingMore ? <p className="feed-progress" role="status"><span className="loader" />正在加载更多…</p>
          : feed.failedToLoadMore ? <ErrorNotice retry={loadMore} label="重试加载更多">{feed.error || '更多内容加载失败，请重试。'}</ErrorNotice>
          : !feed.hasMore ? <p className="end-note">已经看完啦，休息一下吧。<span aria-hidden="true"> ✳</span></p>
          : !feed.error && !busy && supportsObserver ? <p className="end-note">继续向上滑动，发现更多内容</p> : null)}
        {!supportsObserver && feed.loaded && feed.hasMore && !busy && !feed.loadingMore && !feed.error && <button className="load-button" onClick={loadMore}>加载更多</button>}
      </div>
    </section>
  );
}

type Theme = 'system' | 'light' | 'dark';
function readTheme(): Theme {
  try {
    const theme = localStorage.getItem('wanandroid.theme');
    if (theme === 'light' || theme === 'dark') return theme;
  } catch { /* Storage can be unavailable in private browsing. */ }
  return 'system';
}

function Settings({ theme, setTheme }: { theme: Theme; setTheme: (theme: Theme) => void }) {
  const options: { value: Theme; title: string; text: string; icon: IconName }[] = [
    { value: 'system', title: '跟随系统', text: '随设备外观自动切换', icon: 'system' },
    { value: 'light', title: '浅色', text: '明亮、清爽的阅读空间', icon: 'sun' },
    { value: 'dark', title: '深色', text: '适合夜晚的柔和色彩', icon: 'moon' },
  ];
  return <div className="settings-content"><fieldset className="settings-card"><legend>外观主题</legend><div className="theme-options">{options.map((option) => <label key={option.value} className={`theme-option ${theme === option.value ? 'selected' : ''}`}><input type="radio" name="theme" value={option.value} checked={theme === option.value} onChange={() => setTheme(option.value)} /><Icon name={option.icon} /><span><strong>{option.title}</strong><small>{option.text}</small></span></label>)}</div></fieldset><section className="about-card"><div className="brand-mark" aria-hidden="true">W<span>.</span></div><div><h2>WanAndroid</h2><p>发现、分享、实践。和开发者一起持续成长。</p><a href="https://www.wanandroid.com" target="_blank" rel="noopener noreferrer">访问玩 Android <Icon name="arrow" /><span className="sr-only">（在新标签页打开）</span></a></div></section></div>;
}

export function App() {
  const [section, setSection] = useState<Section>(readSection);
  const [theme, setTheme] = useState<Theme>(readTheme);
  const { state, bridgeError, refresh, loadMore, selectProject } = useWanAndroid(section);

  useEffect(() => {
    const update = () => setSection(readSection());
    window.addEventListener('hashchange', update);
    return () => window.removeEventListener('hashchange', update);
  }, []);

  useEffect(() => {
    document.title = `${labels[section]} · WanAndroid`;
    window.scrollTo(0, 0);
  }, [section]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try { localStorage.setItem('wanandroid.theme', theme); } catch { /* The theme still applies for this session. */ }
  }, [theme]);

  return (
    <>
      <a className="skip-link" href="#main-content" onClick={(event) => { event.preventDefault(); document.getElementById('main-content')?.focus(); }}>跳到主要内容</a>
      <header className="site-header">
        <div className="header-inner">
          <a href="#home" className="brand" aria-label="WanAndroid 首页"><span className="brand-mark" aria-hidden="true">W<span>.</span></span><span>WanAndroid<small>开发者的阅读空间</small></span></a>
          <nav aria-label="主要导航">{sections.slice(0, 3).map((item) => <a key={item} href={`#${item}`} className={`nav-link ${section === item ? 'active' : ''}`} aria-current={section === item ? 'page' : undefined}><Icon name={item} /><span>{labels[item]}</span></a>)}</nav>
          <a className={`settings-link ${section === 'settings' ? 'active' : ''}`} href="#settings" aria-label="设置" aria-current={section === 'settings' ? 'page' : undefined}><Icon name="settings" /></a>
        </div>
      </header>

      <main id="main-content" className="main-content" tabIndex={-1}>
        <h1 className="sr-only">{labels[section]}</h1>

        {section === 'settings' ? <Settings theme={theme} setTheme={setTheme} /> : <>
          {bridgeError ? <Placeholder kind="error" title="内容暂时无法显示" message={bridgeError} retry={() => window.location.reload()} />
            : section === 'projects' && state.categories.length === 0 ? (
              state.categoriesError ? <Placeholder kind="error" title="项目分类加载失败" message={state.categoriesError} retry={() => refresh('projects')} />
                : state.categoriesLoading || !state.projects.loaded ? <Placeholder kind="loading" title="正在加载项目分类…" />
                : <Placeholder kind="empty" title="暂无项目分类" message="新的开源灵感还在路上，稍后再来看看吧。" retry={() => refresh('projects')} />
            ) : <>
              {section === 'projects' && <section className="category-section" aria-label="项目分类" aria-busy={state.categoriesLoading}>
                <div className="category-list">{state.categories.map((category) => <button key={category.id} className={`category-button ${category.id === state.selectedProjectId ? 'selected' : ''}`} aria-pressed={category.id === state.selectedProjectId} onClick={() => selectProject(category.id)}>{category.name}</button>)}</div>
              </section>}
              <Feed key={`${section}-${section === 'projects' ? state.selectedProjectId : ''}`} feed={state[section]} section={section} refresh={() => refresh(section)} loadMore={() => loadMore(section)} />
            </>}
        </>}
      </main>

      <footer className="site-footer"><span>保持好奇，持续生长。</span><a href="https://www.wanandroid.com" target="_blank" rel="noopener noreferrer">内容来自玩 Android <Icon name="arrow" /><span className="sr-only">（在新标签页打开）</span></a></footer>
    </>
  );
}
