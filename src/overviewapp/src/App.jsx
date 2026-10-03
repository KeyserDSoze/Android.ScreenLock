import { useEffect, useMemo, useState } from 'react';
import { initialLocale, localeCodes, locales } from './i18n.js';
import { privacyLegal, termsLegal } from './legal.js';

const GITHUB_REPO = 'https://github.com/KeyserDSoze/Android.ScreenLock';
const ISSUES = `${GITHUB_REPO}/issues`;

function LockMark({ compact = false }) {
  return (
    <span className={`lock-mark ${compact ? 'compact' : ''}`} aria-hidden="true">
      <span className="lock-shackle" />
      <span className="lock-body"><span className="lock-dot" /></span>
    </span>
  );
}

function pathFor(segment = '') {
  const base = import.meta.env.BASE_URL;
  return `${base}${segment ? `${segment}/` : ''}`;
}

function currentPage() {
  const basePath = new URL(import.meta.env.BASE_URL, window.location.origin).pathname;
  let path = window.location.pathname;
  if (path.startsWith(basePath)) path = path.slice(basePath.length);
  const segment = path.split('/').filter(Boolean)[0];
  return ['privacy', 'terms', 'contact'].includes(segment) ? segment : 'home';
}

function ThemeButton({ theme, setTheme }) {
  const next = theme === 'dark' ? 'light' : 'dark';
  return (
    <button className="icon-button" type="button" onClick={() => setTheme(next)} aria-label={`Switch to ${next} theme`} title={`Switch to ${next} theme`}>
      {theme === 'dark' ? '☀' : '☾'}
    </button>
  );
}

function Header({ language, setLanguage, theme, setTheme, t, page }) {
  return (
    <header className="site-header">
      <a className="brand" href={pathFor()} aria-label="Screen Lock home">
        <LockMark compact />
        <span>Screen Lock</span>
      </a>
      <nav className="nav-links" aria-label="Main navigation">
        <a className={page === 'home' ? 'active' : ''} href={pathFor()}>{t.nav.home}</a>
        <a className={page === 'privacy' ? 'active' : ''} href={pathFor('privacy')}>{t.nav.privacy}</a>
        <a className={page === 'terms' ? 'active' : ''} href={pathFor('terms')}>{t.nav.terms}</a>
        <a className={page === 'contact' ? 'active' : ''} href={pathFor('contact')}>{t.nav.contact}</a>
      </nav>
      <div className="header-tools">
        <label className="language-picker">
          <span className="sr-only">Language</span>
          <select value={language} onChange={(event) => setLanguage(event.target.value)} aria-label="Language">
            {localeCodes.map((code) => <option key={code} value={code}>{locales[code].name}</option>)}
          </select>
        </label>
        <ThemeButton theme={theme} setTheme={setTheme} />
      </div>
    </header>
  );
}

function PhonePreview() {
  return (
    <div className="phone-shell" aria-label="Illustration of Screen Lock protecting a video call">
      <div className="phone-speaker" />
      <div className="video-grid">
        <div className="video-person one"><span className="face">●</span><span className="body-shape" /></div>
        <div className="video-person two"><span className="face">●</span><span className="body-shape" /></div>
      </div>
      <div className="touch-shield"><LockMark /></div>
      <div className="unlock-target"><span /></div>
      <div className="phone-caption">Touches blocked</div>
    </div>
  );
}

function Home({ t }) {
  return (
    <main>
      <section className="hero shell">
        <div className="hero-copy">
          <div className="eyebrow"><span className="status-dot" /> Android touch protection</div>
          <h1>{t.hero_tagline}</h1>
          <p className="lead">{t.hero_description}</p>
          <div className="hero-actions">
            <a className="button primary" href={GITHUB_REPO}>View on GitHub <span aria-hidden="true">↗</span></a>
            <a className="button secondary" href={pathFor('privacy')}>{t.nav.privacy}</a>
          </div>
          <div className="trust-row">
            <span>No account</span><span>No ads</span><span>No analytics</span><span>40 languages</span>
          </div>
        </div>
        <div className="hero-visual"><PhonePreview /></div>
      </section>

      <section className="section shell">
        <div className="section-heading">
          <span className="eyebrow">How it works</span>
          <h2>One tap to protect the call. One deliberate hold to unlock.</h2>
        </div>
        <div className="feature-grid">
          <article className="feature-card"><span className="feature-number">01</span><h3>Overlay permission</h3><p>{t.setup_step1_desc}</p></article>
          <article className="feature-card"><span className="feature-number">02</span><h3>Quick Settings</h3><p>{t.setup_after_tile}</p></article>
          <article className="feature-card"><span className="feature-number">03</span><h3>Intentional unlock</h3><p>{t.unlock_desc}</p></article>
        </div>
      </section>

      <section className="privacy-band shell">
        <div className="privacy-icon"><LockMark /></div>
        <div>
          <span className="eyebrow">Privacy first</span>
          <h2>{t.privacy_title}</h2>
          <p>{t.privacy_body1}</p>
          <p>{t.privacy_body2}</p>
        </div>
        <a className="button secondary" href={pathFor('privacy')}>{t.nav.privacy} →</a>
      </section>

      <section className="section shell language-section">
        <div>
          <span className="eyebrow">International by design</span>
          <h2>40 languages, matching the Android app.</h2>
          <p>The site follows your browser language by default and lets you switch at any time. Arabic, Persian, Urdu and Hebrew automatically use right-to-left layout.</p>
        </div>
        <div className="language-cloud" aria-label="Supported languages">
          {localeCodes.map((code) => <span key={code}>{locales[code].name}</span>)}
        </div>
      </section>
    </main>
  );
}

function CanonicalLegal({ data, summary, privacy = false, language }) {
  const localizedData = language === 'it' ? data.it : data.en;
  return (
    <main className="legal-page shell">
      <div className="legal-hero">
        <span className="eyebrow">Screen Lock · Legal</span>
        <h1>{localizedData.title}</h1>
        <p className="legal-date">{localizedData.updated}</p>
        <p className="legal-summary">{summary}</p>
        {language !== 'en' && language !== 'it' && <p className="canonical-note">The summary above follows your selected language. The detailed legal text below is the canonical English version and prevails if translations differ.</p>}
      </div>
      <article className="legal-card" dir="ltr">
        <p className="legal-intro">{localizedData.intro}</p>
        {localizedData.sections.map(([heading, body]) => (
          <section key={heading}>
            <h2>{heading}</h2>
            <p>{body}</p>
          </section>
        ))}
        {privacy && <p className="legal-link-line">Official project: <a href={GITHUB_REPO}>{GITHUB_REPO}</a></p>}
      </article>
    </main>
  );
}

function Privacy({ t, language }) {
  return <CanonicalLegal data={privacyLegal} summary={`${t.privacy_body1} ${t.privacy_body2}`} privacy language={language} />;
}

function Terms({ t, language }) {
  return <CanonicalLegal data={termsLegal} summary={t.termsSummary} language={language} />;
}

function Contact({ t }) {
  return (
    <main className="contact-page shell">
      <div className="contact-copy">
        <span className="eyebrow">Screen Lock · Support</span>
        <h1>{t.nav.contact}</h1>
        <p className="lead">Questions, bug reports and privacy requests can be opened on the public project issue tracker. The project is developed and published by Alessandro Rapiti.</p>
        <p className="contact-warning">Do not include passwords, private conversations, health information, addresses, phone numbers, or other sensitive personal information in public GitHub issues.</p>
      </div>
      <div className="contact-cards">
        <a className="contact-card" href={ISSUES}><span className="contact-symbol">◎</span><div><strong>GitHub Issues</strong><span>Support, bugs and privacy questions</span></div><span>↗</span></a>
        <a className="contact-card" href={GITHUB_REPO}><span className="contact-symbol">⌘</span><div><strong>Repository</strong><span>Source code, releases and documentation</span></div><span>↗</span></a>
      </div>
    </main>
  );
}

function Footer({ t }) {
  return (
    <footer className="site-footer shell">
      <div><LockMark compact /><span>{t.footer}</span></div>
      <div className="footer-links">
        <a href={pathFor('privacy')}>{t.nav.privacy}</a>
        <a href={pathFor('terms')}>{t.nav.terms}</a>
        <a href={pathFor('contact')}>{t.nav.contact}</a>
        <a href={GITHUB_REPO}>GitHub ↗</a>
      </div>
      <small>© 2026 Alessandro Rapiti · Screen Lock</small>
    </footer>
  );
}

export default function App() {
  const [language, setLanguage] = useState(() => initialLocale());
  const [theme, setTheme] = useState(() => localStorage.getItem('screenlock-site-theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'));
  const page = useMemo(() => currentPage(), []);
  const t = locales[language] || locales.en;

  useEffect(() => {
    localStorage.setItem('screenlock-site-language', language);
    document.documentElement.lang = language;
    document.documentElement.dir = t.dir;
  }, [language, t.dir]);

  useEffect(() => {
    localStorage.setItem('screenlock-site-theme', theme);
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
  }, [theme]);

  return (
    <div className="app-shell">
      <Header language={language} setLanguage={setLanguage} theme={theme} setTheme={setTheme} t={t} page={page} />
      {page === 'home' && <Home t={t} />}
      {page === 'privacy' && <Privacy t={t} language={language} />}
      {page === 'terms' && <Terms t={t} language={language} />}
      {page === 'contact' && <Contact t={t} />}
      <Footer t={t} />
    </div>
  );
}
