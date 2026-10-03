import { localesA } from './locales-a.js';
import { localesB } from './locales-b.js';
import { localesC } from './locales-c.js';
import { localesD } from './locales-d.js';

export const locales = { ...localesA, ...localesB, ...localesC, ...localesD };
export const localeCodes = Object.keys(locales);

const aliases = { in: 'id', iw: 'he', zh: 'zh-CN' };

export function normalizeLocale(tag) {
  if (!tag) return 'en';
  if (locales[tag]) return tag;
  const normalized = tag.replace('_', '-');
  if (locales[normalized]) return normalized;
  const language = normalized.split('-')[0].toLowerCase();
  if (aliases[language]) return aliases[language];
  return locales[language] ? language : 'en';
}

export function initialLocale() {
  const saved = localStorage.getItem('screenlock-site-language');
  if (saved && locales[saved]) return saved;
  for (const language of navigator.languages || [navigator.language]) {
    const normalized = normalizeLocale(language);
    if (normalized) return normalized;
  }
  return 'en';
}
