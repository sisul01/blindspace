# Blindspace – Steuerungs- und Visualisierungsprototyp

Blindes First-Person-Erkundungsspiel: Die Welt ist unsichtbar und wird nur durch Abtasten mit den Händen
sichtbar. Umgesetzt ist das Konzept `docs/Blindspace_Konzept_v3.md` (Version 3.3). Java 17, AWT/Swing,
keine externen Abhängigkeiten.

## In Eclipse öffnen

1. *File → Import → General → Existing Projects into Workspace*.
2. *Select archive file* (ZIP) oder *Select root directory* (entpackter Ordner `Blindspace`) wählen, Projekt anhaken, *Finish*.
3. Rechtsklick auf `src/blindspace/Main.java` → *Run As → Java Application*.

Voraussetzung: JDK 17 oder neuer. Falls Eclipse die Umgebung `JavaSE-17` nicht findet:
*Project → Properties → Java Build Path → Libraries → JRE System Library → Alternate JRE* wählen.

## Steuerung

| Eingabe | Aktion |
| --- | --- |
| W / S oder Pfeile hoch/runter | vorwärts / rückwärts: kurzer Druck = Halbschritt (25 cm), gehalten = Vollschritte (aufrecht); geduckt: ein Schritt von 10 cm je Tastendruck |
| A / D oder Pfeile links/rechts | Drehung 15° ± 1° pro Tastendruck/Intervall |
| 1–4 | Körperhöhe 90 / 70 / 50 / 30 cm |
| Maus | Hände führen: hoch/runter = Höhe im Tastband, links/rechts = Spannweite |
| linke / rechte Maustaste | nach links / rechts tasten (der andere Arm bleibt vorn) |
| Leertaste oder Num 0 | ausrichten (nur wenn die Anzeige „Ausrichtung möglich" zeigt) |
| F1 / F3 / R | Hilfe ein/aus / Debug-Draufsicht ein/aus (nur zum Testen) / Neustart |

Hinweis: Die Startrichtung ist bei jedem Start und Neustart zufällig. Die Ausrichtung per Leertaste ist manuell. Berührt man dieselbe Fläche zweimal (20–80 cm Abstand, mindestens 0,4 s Zeitabstand, höchstens 20 s), beginnt die Wandführung (keine Drift) und „Ausrichtung möglich" erscheint im HUD. Die Leertaste dreht dann parallel (zuletzt seitlich getastet) oder senkrecht (zuletzt nach vorn getastet) zur Fläche. Zusätzlich richtet sich der Körper beim Loslaufen automatisch parallel zur Wand aus, wenn eine seitlich tastende Hand die Wand berührt und die Blickrichtung höchstens 20° abweicht (dann beginnt auch die Wandführung).

## Aufbau (Pakete unter `src/blindspace`)

Die Pakete sind in Schichten angeordnet; Abhängigkeiten gehen nur von oben nach unten, es gibt keine Zyklen.

| Paket | Klassen | Aufgabe |
| --- | --- | --- |
| `blindspace` | `Main` | Startpunkt: Fenster und Spielschleife |
| `blindspace.anzeige` | `Ansicht`, `Muster` | Darstellung (Perspektive, Tastpunkt-Muster je Oberfläche, Kantenlinien, HUD, Debug-Karte) und Tastatur-/Maus-Listener |
| `blindspace.spiel` | `Spiel` | Spielkern ohne GUI, feste Simulationsschleife (1/120 s), legt die Reihenfolge eines Schritts fest |
| `blindspace.spieler` | `Eingabe`, `Koerper`, `Haende`, `Ausrichtung` | Eingabezustand, Körper (Schritte, Drehung, Drift, Kollision), gespiegelte Hände, manuelle Ausrichtung und Wandführung |
| `blindspace.tasten` | `Tastpunkt`, `Tastpunkte`, `Verblassungsgruppe`, `Tastkante` (nutzt `welt`) | räumliches Gedächtnis: Kurzzeitpunkte (15 s/30 s), Langzeitgruppen (12 min), ertastete Kantenstücke, Kollisionsdämpfung |
| `blindspace.levels` | `Level`, `Testlevel` | Level-Schnittstelle (Welt und Startpose) und das Testlevel; weitere Levels kommen hier hinzu |
| `blindspace.welt` | `Welt`, `Quader`, `Beruehrung`, `Kantenstueck`, `Koerperteil`, `Oberflaeche`, `Objektart` | unsichtbare AABB-Welt, Tastproben, Kantenstücke zu einer Berührung, 3D-Körperkollision |
| `blindspace.konfig` | `Konstanten` | alle Balancing-Werte an einer Stelle (Konzept Abschnitt 12) |

### Neues Level anlegen

1. Neue Klasse im Paket `blindspace.levels` anlegen, die `Level` implementiert (Name, `erzeuge()`, Startpose).
2. In `erzeuge()` eine `Welt` mit `welt.add(name, objektart, oberflaeche, x0, y0, x1, y1, hoehe)` aufbauen (siehe `Testlevel`).
3. Zum Spielen `new Spiel(seed, new MeinLevel())` verwenden (in `Main` oder `SpielMitKarte`).

## Tests (`test/blindspace/test`, ohne JUnit, laufen ohne Fenster)

- `Selbsttest` prüft die Kernregeln des Konzepts (Gedächtnis, Kollision, Ausrichtung, Wandführung, Drift ...).
- `Bildtest` rendert fünf Beispielbilder nach `docs/bilder` (Start, Wand, seitliches Tasten, Kiste geduckt, Kanten am Pfeiler).
- `SpielMitKarte` startet das normal spielbare Spiel mit eingeblendeter Debug-Karte (F3 schaltet sie um).

Alle per Rechtsklick -> *Run As -> Java Application* starten.

## Dokumentation

Alle Klassen, Methoden und Felder sind nach Javadoc-Muster dokumentiert (Deutsch). In Eclipse erscheint die
Beschreibung beim Überfahren mit der Maus oder mit F2.

## Balancing

Alle Werte stehen in `Konstanten.java`, zum Beispiel `TASTINTERVALL`, `SUCHRADIUS_*`, `KZ_DAUER_GEHEN`, `KZ_DAUER_STEHEN`, `LZ_DAUER`, `KOLLISION_FAKTOR_*`, `HAND_LOESEN`,
`LOCK_RELEASE_DIST`, `MAUS_BEREICH` (Mausempfindlichkeit), `DUCK_SCHRITT`, `AUSR_AUTO_MAX_WINKEL`, `TAST_MIN_HOEHE` und die Tastbänder.

## Nicht enthalten (bewusst, siehe Konzept Abschnitt 11)

Spielziel/Win-Bedingung, Tast-Identität (Punktmuster je Objekttyp), Audio, Levelsystem.
