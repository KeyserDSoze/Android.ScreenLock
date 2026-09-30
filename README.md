# Screen Lock

**Screen Lock** è una piccola app Android pensata per un caso molto concreto: lasciare una videochiamata aperta (per esempio Telegram con i nonni) mentre un bambino tiene o tocca il telefono, senza rischiare di chiudere la chiamata o premere pulsanti a caso.

## Come funziona

1. Installa l'APK dalla pagina **Releases**.
2. Apri Screen Lock e segui la configurazione guidata. Android ti chiede di consentire a **Screen Lock** di mostrarsi sopra le altre app. È il permesso speciale `SYSTEM_ALERT_WINDOW`, verificato con `Settings.canDrawOverlays()`.
3. Quando torni nell'app dopo aver concesso il permesso, su Android 13+ Screen Lock richiede automaticamente di aggiungere il pulsante ai **Comandi rapidi**. Sulle versioni precedenti mostra le istruzioni per aggiungerlo manualmente.
4. Avvia la chiamata in Telegram (o qualunque altra app), abbassa i Comandi rapidi e tocca **Screen Lock**.
5. Il pannello viene richiuso; dopo il ritardo configurato (2 secondi di default) parte un foreground service e compare un `TYPE_APPLICATION_OVERLAY` quasi trasparente che assorbe i tocchi e mantiene lo schermo acceso.
6. Il bersaglio di sblocco è visibile per un istante e poi diventa molto discreto. Per sbloccare, tieni premuto **al centro dello schermo** per il tempo configurato (6 secondi di default). Movimento eccessivo, rilascio o multitouch annullano il conteggio.
7. In impostazioni puoi attivare **Protezione tendina (sperimentale)**: usa una Activity trasparente in modalità immersiva per nascondere le barre di sistema e ridurre gli swipe accidentali. Android può comunque rivelare barre transitorie con un gesto dal bordo; un blocco assoluto della System UI richiede modalità device-owner/kiosk.

## Limiti Android

Screen Lock blocca i normali tocchi consegnati alle app sotto l'overlay. Non usa più un Accessibility Service: questo evita le restrizioni aggiuntive applicate da alcune versioni/ROM Android agli APK installati da browser. Un `TYPE_APPLICATION_OVERLAY` resta però sotto le finestre critiche di sistema, quindi tendina notifiche, tasto di accensione, emergenze e alcune UI di sistema rimangono disponibili. Non è una modalità kiosk né modifica Telegram.

## Privacy

- Nessun account, analytics o tracking.
- Internet viene usato solo dall'updater per leggere la release più recente dal repository GitHub e scaricare l'APK ufficiale.
- Nessun servizio di Accessibilità.
- Nessuna lettura del contenuto delle finestre.
- Il foreground service esiste solo mentre il blocco overlay è in attivazione o attivo.
- Preferenze solo locali sul dispositivo.

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

La pipeline conserva la debug keystore nella cache GitHub per rendere normalmente aggiornabili le build successive. Essendo una cache e non una chiave release permanente, se GitHub la elimina potrebbe essere necessario disinstallare una build vecchia prima di installarne una nuova. Per una distribuzione pubblica/Play Store va configurata una vera release keystore tramite GitHub Secrets.

## Struttura

```text
.
├── .github/workflows/release.yml
├── scripts/bump-version.sh
├── VERSION
└── src/
    ├── app/
    │   └── src/main/
    │       ├── java/com/keysersoze/screenlock/
    │       └── res/
    ├── build.gradle.kts
    ├── gradle.properties
    └── settings.gradle.kts
```

## Licenza

MIT.


## Migrazione dalla 0.0.3

Dalla versione 0.0.4 Screen Lock non usa più Accessibilità. Dopo l'aggiornamento apri l'app e concedi **Mostra sopra altre app**; poi usa o aggiungi la tile dei Comandi rapidi. L'eventuale vecchia autorizzazione di Accessibilità non è più necessaria per Screen Lock.


## Aggiornamenti interni

Screen Lock può controllare automaticamente la release più recente su GitHub, scaricare l'APK tramite il Download Manager di Android e aprire l'installer. Android richiede comunque la conferma dell'utente per installare/aggiornare un APK sideloaded e, la prima volta, può richiedere di autorizzare Screen Lock come origine di installazione.

Perché un aggiornamento possa essere installato sopra la versione precedente, tutte le release devono usare la stessa chiave di firma. La workflow di release richiede quindi i repository secrets `SCREENLOCK_KEYSTORE_B64` e `SCREENLOCK_KEYSTORE_PASSWORD`; se mancano, la release fallisce invece di generare un APK con una firma diversa.
