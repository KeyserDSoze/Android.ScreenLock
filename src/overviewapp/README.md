# Screen Lock overview site

React + Vite website published to GitHub Pages.

## Local development

```bash
npm install
npm run dev
```

## Production build

```bash
npm run build
npm run preview
```

The production build uses `/Android.ScreenLock/` as the GitHub Pages base path when built inside GitHub Actions.

Public routes:

- `/` — product overview
- `/privacy/` — privacy policy
- `/terms/` — terms and conditions
- `/contact/` — support/contact links

The UI uses the same 40 languages as the Android app. Marketing and privacy summary copy is generated from the corresponding Android localization resources; English is the canonical legal version, with a full Italian legal version also included.
