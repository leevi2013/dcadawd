# Blue Gamba — Fabric 1.21.11

Ein Minecraft-Ore-Slot-Spiel mit blauem Standard-Chest-GUI, interner Spielwährung und Schutz gegen ungültige Einsätze.

## Was das Mod macht

- Öffnet mit `/gamba` oder der Taste `J` ein blaues 3×9-Chest-GUI. Die Taste lässt sich unter Optionen → Steuerung → Tastenbelegung ändern.
- Zeigt Kohle-, Eisen-, Gold-, Diamant- und Smaragderz sowie Ancient Debris während einer zufälligen Drehung.
- Die Animation dauert 40 bis 80 Server-Ticks, also 2 bis 4 Sekunden.
- Am Ende werden drei Symbole unabhängig gezogen. Nur drei gleiche Symbole zahlen den jeweiligen Multiplikator aus; bei keinem Dreier ist der Einsatz verloren.
- Startguthaben: 1.000,0 interne Coins pro Spieler. Das Guthaben bleibt in der Welt gespeichert; es gibt keine Echtgeld- oder externe Economy-Anbindung.
- Einsatz über die GUI mit -10, -1, +1 und +10 oder per Chatbefehl festlegen.
- Guthaben und Einsatz sind in Coins sichtbar. Die interne Guthabenauflösung verwendet Zehntel-Coins, damit 0,5x und 1,2x exakt abgerechnet werden.

## Auszahlungen

Die Auszahlung ist der Gesamtbetrag, der zurückgebucht wird. Der zuvor eingesetzte Betrag ist darin enthalten.

| Drei gleiche Symbole | Multiplikator | Beispiel bei 10 Coins |
|---|---:|---:|
| Kohleerz | 0,5x | 5 Coins zurück |
| Eisenerz | 1,2x | 12 Coins zurück |
| Golderz | 2,0x | 20 Coins zurück |
| Diamanterz | 5,0x | 50 Coins zurück |
| Smaragderz | 10,0x | 100 Coins zurück |
| Netherit-Schrott / Ancient Debris | 25,0x | 250 Coins zurück |

Jedes der sechs Symbole hat dieselbe Wahrscheinlichkeit. Auch Kohle ergibt drei gleiche Symbole und zahlt 0,5x aus; der Nettogewinn ist dann -50 % des Einsatzes.

## Befehle

- `/gamba` — öffnet die Slot-Maschine.
- `J` — öffnet das Gamba-GUI im Spiel; die Taste kann in den Steuerungseinstellungen geändert werden.
- `/gamba bet <coins>` — setzt einen ganzzahligen Einsatz, z. B. `/gamba bet 25`.
- `/gamba balance` — zeigt das Guthaben.
- `/gamba bet 0` oder ein negativer Betrag — zeigt eine Chatfehlermeldung, setzt den GUI-Einsatz auf ungültig und sperrt den Drehknopf. Mit einem positiven Befehl oder den Plus-Tasten wird die Sperre aufgehoben.
- Maximaler Einsatz: 100.000 Coins. Ein Einsatz über dem aktuellen Guthaben wird beim Drehen abgelehnt.

## Voraussetzungen

- Minecraft Java Edition 1.21.11
- Java/JDK 21
- Gradle 9.2.1
- Fabric Loader 0.18.6 für Minecraft 1.21.11
- Fabric API 0.141.6+1.21.11

## Lokal bauen

1. Installiere JDK 21 und Gradle 9.2.1.
2. Öffne ein Terminal im Ordner dieses Projekts.
3. Führe aus:

~~~powershell
gradle build
~~~

4. Die Mod-Datei liegt anschließend in `build/libs/blue-gamba-1.0.1.jar`.
5. Installiere Fabric Loader und Fabric API für Minecraft 1.21.11. Lege die JAR in den `mods`-Ordner. Für eine Multiplayer-Welt muss das Mod auf dem Server vorhanden sein; installiere es am einfachsten auch im Client.

Nach einem Push baut GitHub Actions das Projekt automatisch. Öffne im Repository den Tab **Actions**, wähle den neuesten Lauf **Build Mod** und lade unter **Artifacts** die Mod-JAR herunter.

## Mit GitHub im Browser hochladen

Du brauchst GitHub Desktop und Git nicht, um den Quellcode hochzuladen.

1. Lade die ZIP-Datei des Projekts herunter und entpacke sie mit dem Windows-Explorer. Öffne den entpackten Ordner `blue-gamba-fabric`.
2. Melde dich auf [GitHub.com](https://github.com) an. Klicke oben rechts auf **+ → New repository**.
3. Repository-Name: `blue-gamba-fabric`. Wähle **Public** oder **Private**.
4. Lass **Add a README file**, **Add .gitignore** und **Choose a license** ausgeschaltet. Diese Projektdateien sind schon vorhanden.
5. Klicke **Create repository**.
6. Im neuen leeren Repository klicke **Add file → Upload files**.
7. Ziehe den Inhalt des entpackten Projektordners in das Upload-Feld oder wähle die Projektdateien dort aus. Lade den Ordnerinhalt hoch, nicht die ZIP-Datei. Achte darauf, dass auch der Ordner `.github/workflows` mit `main.yml` und die Datei `.gitignore` dabei sind.
8. Als Commit-Nachricht kannst du `Initial Blue Gamba mod` eintragen. Wähle **Commit directly to the main branch** und klicke **Commit changes**.
9. Öffne im Repository den Tab **Actions**. Der Workflow **Build Mod** startet nach dem Commit automatisch. Falls GitHub dich auffordert, Workflows erst zu aktivieren oder zu bestätigen, folge dem Hinweis.
10. Öffne den neuesten erfolgreichen **Build Mod**-Lauf. Lade unter **Artifacts** das Artefakt `blue-gamba-fabric-minecraft-1.21.11` herunter und entpacke es. Darin liegt die Mod-JAR.

GitHub dokumentiert das [Erstellen eines Repositories](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-new-repository) und das [Hochladen von Dateien im Browser](https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository). Der Browser-Upload unterstützt bis zu 100 Dateien pro Upload und 25 MiB pro Datei; dieses Projekt liegt deutlich darunter.

### Mod installieren

Installiere Fabric Loader für Minecraft Java Edition 1.21.11 und lege die heruntergeladene JAR in den `mods`-Ordner. Fabric API 0.141.6+1.21.11 wird ebenfalls benötigt. Für Multiplayer muss die Mod auf dem Server installiert sein; installiere sie am besten auch beim Client.


## Projektstruktur

~~~text
blue-gamba-fabric/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── README.md
├── .github/workflows/main.yml
└── src/main/
    ├── java/de/codex/bluegamba/
    │   ├── BlueGambaClient.java
    │   ├── BlueGambaMod.java
    │   ├── GambaScreenHandler.java
    │   ├── GambaService.java
    │   └── OreSymbol.java
    └── resources/fabric.mod.json
~~~

Die Beschriftungen sitzen als Item-Namen in den jeweiligen GUI-Slots und erscheinen beim Darüberfahren. Dadurch liegen keine dauerhaft gezeichneten Textzeilen übereinander; das Chest-GUI behält seinen normalen Abstand und seine Standardpositionen.
