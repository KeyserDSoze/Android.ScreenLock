# Screen Lock

**Screen Lock** è una piccola app Android pensata per un caso molto concreto: lasciare una videochiamata aperta (per esempio Telegram con i nonni) mentre un bambino tiene o tocca il telefono, senza rischiare di chiudere la chiamata o premere pulsanti a caso.

## Come funziona

1. Installa l'APK dalla pagina **Releases**.
2. Apri Screen Lock e segui la configurazione guidata. Android ti chiede di consentire a **Screen Lock** di mostrarsi sopra le altre app. È il permesso speciale `SYSTEM_ALERT_WINDOW`, verificato con `Settings.canDrawOverlays()`.
3. Quando torni nell'app dopo aver concesso il permesso, su Android 13+ Screen Lock richiede automaticamente di aggiungere il pulsante ai **Comandi rapidi**. Sulle versioni precedenti mostra le istruzioni per aggiungerlo manualmente.
4. Avvia la chiamata in Telegram (o qualunque altra app), abbassa i Comandi rapidi e tocca **Screen Lock**.
5. Il pannello viene richiuso; dopo il ritardo configurato (2 secondi di default) parte un foreground service e compare un `TYPE_APPLICATION_OVERLAY` quasi trasparente che assorbe i tocchi e mantiene lo schermo acceso.
6. Il bersaglio di sblocco è visibile per un istante e poi diventa molto discreto. Posizione e raggio sono configurabili; per sbloccare, tieni premuto sul bersaglio per il tempo configurato (da 2 a 12 secondi; 6 secondi di default). Movimento eccessivo, rilascio o multitouch annullano il conteggio.
7. In impostazioni puoi attivare **Protezione tendina avanzata**. È opzionale e richiede un servizio di Accessibilità separato, limitato agli eventi di `com.android.systemui`: mentre Screen Lock è attivo usa `GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE` per chiedere ad Android di richiudere notifiche e Comandi rapidi appena si aprono.

## Limiti Android

Screen Lock blocca i normali tocchi consegnati alle app sotto l'overlay. Il lock di base non richiede Accessibilità. La Protezione tendina avanzata usa invece un Accessibility Service opzionale che non recupera il contenuto delle finestre (`canRetrieveWindowContent=false`) ed è filtrato su `com.android.systemui`. Su Android 13+ un APK installato fuori dallo store può essere soggetto a “Restricted Settings”; in quel caso Android richiede un consenso aggiuntivo e alcune ROM possono non renderlo disponibile. Power, emergenze e altre UI critiche restano comunque sotto il controllo del sistema.

## Privacy

- Nessun account, analytics o tracking.
- Internet viene usato solo dall'updater per leggere la release più recente dal repository GitHub e scaricare l'APK ufficiale.
- Il lock di base non usa Accessibilità.
- La Protezione tendina avanzata usa un servizio opzionale, limitato a System UI e con lettura del contenuto disattivata.
- Nessuna lettura del contenuto delle finestre.
- Il foreground service esiste solo mentre il blocco overlay è in attivazione o attivo.
- Preferenze solo locali sul dispositivo.

## Branding

Il rebranding runtime usa solo risorse vettoriali: la home è disegnata in Jetpack Compose, il launcher usa un adaptive icon vettoriale con variante monochrome per Android 13+, e tile dei Comandi rapidi/notifica foreground condividono un simbolo monocromatico coerente. Gli artwork raster sperimentali restano nel repository come concept, ma non vengono caricati dall'app.

## Punto di sblocco personalizzabile

Il punto di sblocco può essere configurato con un raggio da 40 a 120 dp e posizionato in una griglia 3×3: alto sinistra, alto centro, alto destra, centro sinistra, centro, centro destra, basso sinistra, basso centro e basso destra. L'overlay mantiene automaticamente un margine di sicurezza dai bordi di sistema.

Launcher, home, tile e icona di notifica sono ora tutti vettoriali; nessun asset WebP/JPEG viene caricato durante l'avvio.

## Lingue

Screen Lock segue per impostazione predefinita la lingua del sistema. Dalla schermata principale è possibile scegliere manualmente una lingua diversa; la preferenza resta salvata e, su Android 13+, viene sincronizzata anche con le lingue per-app del sistema.

Lingue incluse (40): English, Italiano, Español, Français, Deutsch, Português, Русский, العربية, हिन्दी, 简体中文, 日本語, 한국어, Bahasa Indonesia, Türkçe, Tiếng Việt, বাংলা, اردو, فارسی, Polski, Nederlands, ไทย, Bahasa Melayu, Kiswahili, தமிழ், తెలుగు, मराठी, ਪੰਜਾਬੀ, ગુજરાતી, ಕನ್ನಡ, മലയാളം, မြန်မာ, नेपाली, Українська, עברית, Ελληνικά, Română, Čeština, Magyar, Svenska e Hausa. Se la lingua di sistema non è tra quelle supportate, il fallback è l'inglese.

La localizzazione copre UI principale, onboarding, updater, notifiche del foreground service, testo di sblocco, tile dei Comandi rapidi e descrizioni del servizio opzionale di Accessibilità.

## Google Play

La pipeline genera due distribuzioni con lo stesso package e la stessa identità di firma:

- **APK GitHub**: include updater GitHub e protezione tendina opzionale.
- **AAB Google Play**: variante Play-safe senza auto-updater, `INTERNET`, `REQUEST_INSTALL_PACKAGES` o AccessibilityService; mantiene il lock overlay, tile, foreground service, personalizzazione e localizzazioni.

Documentazione di pubblicazione:

- [PLAY_STORE.md](PLAY_STORE.md) — setup una tantum, firma Play e pipeline.
- [PLAY_CONSOLE_ANSWERS.md](PLAY_CONSOLE_ANSWERS.md) — risposte preparate per Data safety, App access, foreground service, listing e reviewer.
- [PRIVACY.md](PRIVACY.md) — privacy policy pubblica.

La pubblicazione automatica è intenzionalmente limitata al track **Internal testing** e si attiva soltanto dopo aver configurato il Play Console e la variabile GitHub `GOOGLE_PLAY_ENABLED=true`.

## Sviluppo

Il progetto Android vive in [`src/`](src/) e usa lo stack stabile corrente:

- compile/target SDK 37
- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- Kotlin 2.4.10 con built-in Kotlin di AGP
- Jetpack Compose BOM 2026.09.00 + Material 3
- minSdk 26

Per compilare localmente, apri `src/` con Android Studio oppure usa Gradle 9.6 con JDK 17 e Android SDK 37:

```bash
cd src
gradle :app:testDebugUnitTest :app:assembleDebug
```

L'APK viene prodotto in `src/app/build/outputs/apk/debug/app-debug.apk`.

## Versioni e release

La versione è conservata nel file [`VERSION`](VERSION). Per incrementarla:

```bash
./scripts/bump-version.sh patch   # oppure minor / major
git add VERSION
git commit -m "chore: bump version"
git push
```

Quando un nuovo `VERSION` arriva su `main`, la GitHub Action:

- valida che il tag `vX.Y.Z` non esista già;
- esegue i test JVM della logica di sblocco;
- compila un APK debug **installabile**;
- calcola SHA-256;
- crea automaticamente la GitHub Release e allega APK + checksum.

La pipeline richiede una keystore stabile tramite i GitHub Secrets `SCREENLOCK_KEYSTORE_B64` e `SCREENLOCK_KEYSTORE_PASSWORD`, verifica il certificato con `apksigner` e rifiuta di pubblicare se la firma non è disponibile. Questo rende aggiornabili tra loro le release dalla v0.0.5 in avanti.

## Struttura

```text
.
├── .github/workflows/release.yml
├── scripts/bump-version.sh
├── VERSION
└── src/
    ├── app/
    │   └── src/main/
    │       ├── java/com/keyserdsoze/screenlock/
    │       └── res/
    ├── build.gradle.kts
    ├── gradle.properties
    └── settings.gradle.kts
```

## Licenza

MIT.


## Protezione tendina avanzata

Il blocco touch continua a funzionare con il solo permesso **Mostra sopra altre app**. Se vuoi anche contrastare gli swipe sulla tendina, abilita **Protezione tendina avanzata**: l'app spiega prima cosa viene attivato e poi apre Accessibilità. La voce da cercare è **Screen Lock · Protezione tendina**.

Se Android mostra “Controlled by Restricted Setting”, apri **Info app** e cerca **Consenti impostazioni con limitazioni**. Questa autorizzazione è gestita da Android e può variare in base alla ROM; l'app non può aggirarla automaticamente.


## Aggiornamenti interni

Screen Lock può controllare automaticamente la release più recente su GitHub, scaricare l'APK tramite il Download Manager di Android e aprire l'installer. Android richiede comunque la conferma dell'utente per installare/aggiornare un APK sideloaded e, la prima volta, può richiedere di autorizzare Screen Lock come origine di installazione.

Perché un aggiornamento possa essere installato sopra la versione precedente, tutte le release devono usare la stessa chiave di firma. La workflow di release richiede quindi i repository secrets `SCREENLOCK_KEYSTORE_B64` e `SCREENLOCK_KEYSTORE_PASSWORD`; se mancano, la release fallisce invece di generare un APK con una firma diversa.
