# Screen Lock

**Screen Lock** è una piccola app Android pensata per un caso molto concreto: lasciare una videochiamata aperta (per esempio Telegram con i nonni) mentre un bambino tiene o tocca il telefono, senza rischiare di chiudere la chiamata o premere pulsanti a caso.

## Come funziona

1. Installa l'APK dalla pagina **Releases**.
2. Apri Screen Lock e abilita il suo servizio di accessibilità. Il servizio **non legge il contenuto dello schermo** (`canRetrieveWindowContent=false`): usa il permesso per creare un `TYPE_ACCESSIBILITY_OVERLAY` touchable sopra l'app corrente.
3. Aggiungi **Screen Lock** ai Comandi rapidi Android dall'app o dal pannello di modifica delle tile.
4. Avvia la chiamata in Telegram (o qualunque altra app), abbassa i Comandi rapidi e tocca **Screen Lock**.
5. Il pannello viene richiuso e compare un overlay quasi trasparente che assorbe i tocchi e mantiene lo schermo acceso.
6. Per sbloccare, tieni premuto **al centro dello schermo** per il tempo configurato (6 secondi di default). Se sposti troppo il dito o lo sollevi, il conteggio si annulla.

## Limiti Android

Screen Lock blocca i normali tocchi consegnati alle app sotto l'overlay. Android conserva intenzionalmente alcune vie di sicurezza che una normale app non può neutralizzare del tutto: tasto di accensione, emergenze e alcune interazioni della UI di sistema possono restare disponibili a seconda del dispositivo/ROM. Non è una modalità kiosk né modifica Telegram.

## Privacy

- Nessun permesso Internet.
- Nessun account, analytics o tracking.
- Nessuna lettura del contenuto delle finestre.
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
gradle :app:assembleDebug
```

L'APK viene prodotto in `src/app/build/outputs/apk/debug/app-debug.apk`.

## Versioni e release

La versione è conservata nel file [`VERSION`](VERSION), inizialmente `0.0.1`. Per incrementarla:

```bash
./scripts/bump-version.sh patch   # oppure minor / major
git add VERSION
git commit -m "chore: bump version"
git push
```

Quando un nuovo `VERSION` arriva su `main`, la GitHub Action:

- valida che il tag `vX.Y.Z` non esista già;
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
