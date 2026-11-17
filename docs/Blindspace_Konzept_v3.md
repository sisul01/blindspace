# Blindspace – Spielkonzept, Version 3.4 (bereinigt)

> Dieses Dokument beschreibt das Spielkonzept „Blindspace" (früher „Der Ausgang") vollständig, inklusive aller Entscheidungen aus der Prototyp-Iteration und der anschließenden Konzeptprüfung. Es ist als Eingabe (KI-Prompt) für die nächste Iteration formuliert. Ein existierender Java-Prototyp (Eclipse, Java 17, AWT/Swing, keine externen Abhängigkeiten) bestätigt die Spielbarkeit der Mechanik. Diese Fassung ersetzt Version 1 und 2 und ist die verbindliche Basis. Das übergeordnete Spielkonzept steht im „Blindspace – Game Design Document" (Version 1.0); dieses Dokument legt die Steuerung und das Gedächtnismodell des Prototyps fest und weist Abweichungen zum GDD aus (Abschnitt 15).

## 0. Änderungen gegenüber Version 1

| Abschnitt | Änderung |
| --- | --- |
| 3 | Begründung des 80-cm-Beinzylinders ergänzt; vertikale Anordnung des Körpers festgelegt. |
| 4.3 | Neue Tastbänder: Gehen 130–160 cm; Stehen je Körperhöhe 140–180 / 100–140 / 60–100 / 20–60 cm. Definition von „Gehen" und „Stehen". Gehen nur aufrecht erlaubt. |
| 4.4 | Hand „klebt" bei anhaltendem Kontakt an der Kontaktstelle, bis die Zielposition um 10 cm abweicht. |
| 4.5 | Doppelregel zur Ausrichtung entfernt, Verweis auf Abschnitt 7. |
| 6 | Rückwärtsbewegung bei Körperkollision: halber Schritt (25 cm) statt 50 cm. Helligkeitsreduktion als Multiplikator. |
| 7 | Komplett überarbeitet (v2.2: **manuelle** Ausrichtung): Tastpaar (gleiche Fläche, 20–80 cm, 0,4–20 s) macht die Ausrichtung möglich und startet die Wandführung; ausgeführt wird sie nur per Leertaste/Num 0, parallel oder senkrecht nach der letzten Tastrichtung. Keine automatische Ausführung, keine Entlastungspause. |
| 5 | (v2.3) Einzeldruck = Halbschritt (25 cm), gehaltene Taste = Vollschritte bis zum Loslassen; Drehschritt 15° ± 1°; zufällige Startrichtung. |
| 6 | (v3.0) Kollision erzeugt einen Tastpunkt (Regeln des Gehens). Dämpfungsfaktoren je Körperteil: Beine 0,7 / Rumpf 0,55 / Kopf 0,4. |
| 8.3 | (v3.0) Zweistufiges Gedächtnis: Kurzzeit (Gehen 15 s, Stehen 30 s auf 2,5 %), Langzeit mit Verblassungsgruppen (12 min). Die Löschung nach 35 s und die Einheits-Konstante k entfallen. |
| 8.1 | (v3.1) Oberflächenarten rau / glatt / strukturiert; Darstellung ausschließlich weiß auf schwarz, Oberfläche zeigt sich im Muster. |
| 7.5 | (v3.2) Neu: **automatische Parallelausrichtung** beim Loslaufen, wenn eine seitlich tastende Hand eine senkrechte Fläche berührt und die Blickrichtung höchstens 20° von der Flächenrichtung abweicht. Sie startet auch die Wandführung. Die Ausrichtung per Leertaste bleibt. |
| 4.3 | (v3.2) Tiefstes Tastband beginnt bei 5 cm statt 20 cm (Stufen ertastbar); Maximum bleibt 180 cm. Der Boden selbst und Gegenstände unterhalb des Bandes sind nicht ertastbar. |
| 4.3, 5 | (v3.2) Geduckt (Körperhöhen 70 / 50 / 30 cm) sind Einzelschritte von 10 cm vorwärts und rückwärts möglich; bisher war Gehen geduckt gesperrt. |
| 8.5 | (v3.4) Darstellung geändert: schwarze Linie, 2 Punkte breit, beim Verblassen aufgehellt (gegensätzlich zu den Kugeln). (v3.3) Neu: **Kanten.** Berührt eine Hand eine Quaderfläche höchstens 5 cm von einer Kante entfernt, wird ein 40 cm langes Kantenstück als Linie sichtbar (schwarz, über den Tastpunkten). Kanten werden bei jedem Simulationsschritt geprüft, verblassen wie Tastpunkte und werden bei erneuter Berührung aufgefrischt. Abweichung vom GDD: Linien statt Kugeln (Abschnitt 15.2). |
| 15 | Neu in v3.0: Mehrgeschossigkeit (Planung) und Abgleich mit dem Blindspace-GDD. |
| 9 | Ablauf bei niedrigen Objekten neu beschrieben; Erreichbarkeitstabelle ergänzt (korrigiert in v2.1); Pfeiler auf 1,6 m erhöht; Tippfehler und Widerspruch entfernt. |
| 14 | Neu in v2.1: Umsetzungsentscheidungen des Eclipse-Prototyps. |
| 12 | Neu: zentrale Konstanten für das Balancing. |
| 13 | Auftrag an Version 2 angepasst (früher Abschnitt 12). |

## 1. Kurzbeschreibung

Ein First-Person-Erkundungsspiel, in dem der Spieler **blind** ist. Die Welt ist unsichtbar; sie wird ausschließlich durch Abtasten mit den Händen erschlossen. Jede ertastete Stelle erzeugt einen kleinen, lichtschwachen Visualisierungspunkt, der verblasst. Ziel: einen Ausgang finden. Bewusst kein Audio, kein Licht, keine Minimap – die räumliche Orientierung entsteht nur aus dem Gedächtnis des Spielers und der ephemeren Tastpunkt-Wolke.

Die aktuelle Entwicklung ist ein Steuerungs- und Visualisierungstest: Spielziel/Win-Bedingungen, Levelgenerierung und Audio sind explizit **nicht** Teil des Scopes.

## 2. Koordinatensystem und Einheiten

- Einheiten: Meter (SI). Winkel intern in Radiant, Konzeptwerte in Grad.
- Achsen: X = rechts, Y = vorwärts, Z = hoch. Nullpunkt Z0 im Zentrum des Fußkreises auf Bodenhöhe.
- Spielerebene: Position = Mittelpunkt Beinzylinder in der XY-Ebene; Blickrichtung = Winkel (0 = +Y, positiv gegen den Uhrzeigersinn).
- Kollisionsgeometrie der Welt: ausschließlich achsenparallele Quader (AABBs). Der Boden ist eine virtuelle Ebene z = 0. Jedes Objekt hat eine stabile numerische ID; der Boden hat die reservierte ID −1.
- Jede Berührung liefert neben der ID auch die **Fläche** (die getroffene AABB-Seite, also ihre Normale ±X, ±Y oder ±Z).

## 3. Kollisionsbody

Der Spielerkörper ist unsichtbar und wird intern durch drei Kollisionsformen repräsentiert (von unten nach oben):

| Körperteil | Form | Abmessungen |
| --- | --- | --- |
| Füße/Beine | vertikaler Zylinder | Durchmesser 80 cm, Höhe variabel 30–90 cm |
| Hüfte/Oberkörper | Quader, zentriert über dem Zylinder | X: 40 cm, Y: 30 cm, Z: 60 cm |
| Hals/Kopf | vertikaler Zylinder | Durchmesser 20 cm, Höhe 20 cm |

**Begründung des Beinzylinders:** Der Durchmesser von 80 cm ist Absicht. Der Zylinder bildet die Schrittlänge ab, denn bei einer Kollision berührt meist zuerst der Fuß das Objekt. Er ist deshalb deutlich breiter als der Oberkörper.

**Vertikale Anordnung (bestätigt):** Der Beinzylinder reicht von z = 0 bis zur Körperhöhe h. Darüber folgt der Oberkörper (60 cm), darüber der Kopf (20 cm). Die Kollisionsprüfung der Welt erfolgt dreidimensional gegen alle drei Formen. Alle Weltobjekte stehen auf dem Boden, deshalb berühren niedrige Objekte immer zuerst die Beine.

- Bewegung über Tastatur in der XY-Ebene.
- **Körperhöhe in 4 Stufen** (Tasten 1–4): Beinzylinder 90 / 70 / 50 / 30 cm. Der Tastbereich der Hände verschiebt sich mit der Körperhöhe nach unten (siehe 4.3).
- Bewegung: vorwärts/rückwärts; Drehung in Schritten (siehe 5).

## 4. Hände und Tasten

### 4.1 Aufbau

- Zwei Hände, **synchron gespiegelt**; der Mauszeiger ist an die Hände gekoppelt (nicht an die Kamera – die Blickrichtung ist fest an die Körperausrichtung gebunden).
- Arme befestigt oben an beiden Seiten des Oberkörper-Quaders. Armlänge: min. 25 cm, max. 65 cm; ohne Objektkontakt voll ausgestreckt.
- Die Hände speichern ihre Position im Körper-Koordinatensystem und werden daraus jeden Frame mit dem aktuellen Blickwinkel in Weltkoordinaten umgerechnet. Folge: **Die Hände drehen immer exakt mit dem Körper mit**, behalten ihre relative Position und sind stets im Sichtfeld.
- Startrichtung: bei jedem Start und Neustart **zufällig** (Position im Raum 1 fest).
- Startpose: beide Hände halb ausgestreckt vor dem Körper auf Brusthöhe, z = 145 cm (liegt in beiden Bändern: Gehen 130–160, aufrechtes Stehen 140–180). Ab Frame 1 sichtbar.
- Da der Beinradius (40 cm) kleiner ist als die Handreichweite (Oberkörpertiefe/2 + Armlänge ≈ 80 cm), berührt die Hand hohe Objekte vor den Beinen. Niedrige Objekte unterhalb des Tastbandes werden zuerst von den Beinen berührt.

### 4.2 Maussteuerung

| Eingabe | Wirkung |
| --- | --- |
| Maus hoch/runter | Hände in Z-Achse (innerhalb des Tastbandes) |
| Maus links/rechts | Hände in X-Achse (Spannweite) |
| Keine Maustaste | beidhändiges Tasten nach vorn, synchron gespiegelt |
| Linke Maustaste gedrückt | nach links tasten; rechter Arm nach vorn gestreckt |
| Rechte Maustaste gedrückt | nach rechts tasten; linker Arm nach vorn gestreckt |

Die Y-Komponente (Arm nach vorn) wird vom System gesteuert. Beim Tasten nach links/rechts tastet die jeweilige Hand seitlich, während der andere Arm nach vorn gestreckt bleibt (Kollisionsabschätzung nach vorn).

### 4.3 Tastmodi und Tastbänder

**Definition Gehen/Stehen:** „Gehen" gilt, solange ein Schritt läuft oder eine Gehtaste gehalten wird, plus **0,3 s Nachlauf** (verhindert Flackern des Modus). In allen anderen Fällen, auch bei reiner Drehung und bei Duckschritten (siehe unten), gilt „Stehen".

| | Tasten im Gehen | Abtasten im Stehen |
| --- | --- | --- |
| Suchradius der Hand | 7 cm (größer, grob) | 3 cm (klein, präzise) |
| Tastpunkt-Visualisierung | halber Durchmesser: 10 cm | voller Durchmesser: 20 cm |
| Starthelligkeit | 50 % | 100 % |

**Tastband der Hände (Z-Bereich, in cm):**

| Modus | Körperhöhe h | Tastband | Spanne |
| --- | --- | --- | --- |
| Gehen | 90 | 130–160 | 30 |
| Stehen | 90 | 140–180 | 40 |
| Stehen | 70 | 100–140 | 40 |
| Stehen | 50 | 60–100 | 40 |
| Stehen | 30 | 5–60 | 55 |

Im Gehen wird zusätzlich automatisch nach vorn getastet (Kollisionsvermeidung).

**Tiefste Tasthöhe (v3.2).** Das tiefste Band beginnt bei 5 cm (Konstante `TAST_MIN_HOEHE`), damit niedrige Stufen (Abschnitt 15.1) ertastbar sind. Das Maximum bleibt 180 cm. Der **Boden selbst ist nie ertastbar**, ebenso wenig Gegenstände am Boden, die unter dem Band liegen: Die Probenkugel (Suchradius 3 cm im Stehen) reicht von der tiefsten Handhöhe bis etwa 2 cm über den Boden. Die Hände ignorieren den Boden also ausdrücklich; die Tastprobe der Welt kennt ihn weiterhin (ID −1).

**Gehen (Vollschritte und Halbschritte) ist nur aufrecht (h = 90 cm) erlaubt.** Bei geduckter Haltung (h < 90 cm) gibt es stattdessen **Einzelschritte von 10 cm** vorwärts und rückwärts (W/S, Pfeiltasten hoch/runter; Einzelheiten in Abschnitt 5). Drehen (A/D) bleibt in allen Körperhöhen möglich. Wechselt der Spieler während des Gehens auf eine niedrigere Stufe, wird der laufende Schritt noch beendet.

### 4.4 Tastrate und Ereignislogik

- Pro Hand entsteht höchstens **ein Tastpunkt alle 0,5 Sekunden**, auch bei anhaltendem Kontakt.
- Tastpunkte (und die zugehörige Visualisierung) entstehen ausschließlich bei echter Berührung mit einem Objekt (Kugel-Probe gegen die AABB-Welt).
- **Trifft nur eine Hand ein Objekt, stoppen beide Hände synchron.** Die freie Hand läuft nicht weiter.
- **Hand klebt:** Bei anhaltendem Kontakt bleibt die Hand an der Kontaktstelle, bis die Zielposition der Hand (aus der Mausbewegung) um mindestens 10 cm von der Kontaktstelle abweicht, seitlich oder von der Fläche weg. Bewegung in das Objekt hinein löst die Hand nicht. Die Punkte bei anhaltendem Kontakt entstehen weiter im 0,5-s-Takt am selben Ort.

### 4.5 Handkontakt beim Gehen

Trifft die nach vorn zeigende Hand beim Gehen auf ein Objekt: Der Körper bleibt stehen, ein **Tastereignis** wird ausgelöst (kein Kollisionsereignis). Trifft die seitlich tastende Hand eine Wand, läuft der Körper weiter; das Tastereignis wird protokolliert und kann ein Tastpaar bilden und die Ausrichtung möglich machen (siehe Abschnitt 7). Beim Loslaufen richtet sich der Körper in diesem Fall außerdem automatisch parallel zur Wand aus (Abschnitt 7.5).

## 5. Bewegung und Zufallsdrift

Realitätsprinzip: Niemand hält exakt eine Gerade oder dreht exakt um einen Winkel.

- Schritt: 50 cm Strecke; Gehgeschwindigkeit ca. 1,1 m/s.
- **Tastendauer:** Wird die Gehtaste (vorwärts/rückwärts) gehalten, folgen kontinuierlich Vollschritte bis zum Loslassen; ein begonnener Schritt wird zu Ende geführt. Wird die Taste bei einem **kurzen Einzeldruck** losgelassen, bevor der halbe Schritt (25 cm) erreicht ist, endet der Schritt als **Halbschritt (25 cm)**. Nur der erste Schritt eines Tastendrucks kann ein Halbschritt sein.
- **Duckschritte (v3.2):** Geduckt (Körperhöhen 70 / 50 / 30 cm) ergibt jeder Tastendruck von W/S (oder Pfeil hoch/runter) genau **einen Schritt von 10 cm** vorwärts oder rückwärts. Eine gehaltene Taste wiederholt den Schritt nicht; für den nächsten Schritt muss die Taste neu gedrückt werden. Duckschritte gelten als „Stehen“ (Suchradius 3 cm, Punkte mit 20 cm und 100 %), haben keine Drift und zählen nicht für den Driftzähler. Die vordere Hand blockiert den Schritt vorwärts wie beim Gehen (4.5), und eine Körperkollision wirkt wie gewohnt (Abschnitt 6). Die Schritte dienen dazu, sich einem Objekt in kleinen Stufen zu nähern und es abzutasten.
- **Drift:** Nach den ersten 10 Schritten erhält jeder weitere Schritt eine zufällige Winkelabweichung von 1–2°, immer auf derselben Seite (Seite wird pro Session zufällig festgelegt). Die Abweichungen stapeln sich.
- Die Drift ist abgeschaltet, solange die **Wandführung** aktiv ist (Abschnitt 7.4). Nach dem Ende der Wandführung wird der Schrittzähler auf 0 gesetzt; die Drift beginnt erst wieder nach 10 weiteren Schritten.
- **Drehung:** in Schritten von 15° ± 1° zufällig, im Stehen oder Gehen, animiert (~0,35 s).
- Nach einer Körperkollision gibt es eine kurze Eingabesperre (~0,6 s), um Ereignisflut zu vermeiden.

## 6. Kollisionen

- **Handkontakt:** normales Tastereignis (siehe 4.5). Keine Kollisionsfolgen.
- **Körperkontakt** (Beine, Oberkörper-Ecken, Kopf gegen Welt): **Kollisionsereignis**. Ablauf:
  1. Der Body macht einen **halben Schritt (25 cm) rückwärts**. Würde er dadurch ein weiteres Hindernis berühren, bleibt er stehen.
  2. Der Body bleibt stehen (Eingabesperre ~0,6 s).
  3. Die Helligkeit **aller aktiven Visualisierungen** (Kurzzeitpunkte und Verblassungsgruppen) wird mit dem **Faktor des getroffenen Körperteils** multipliziert: **Beine 0,7, Rumpf 0,55, Kopf 0,4**. Mehrere Kollisionen wirken kumulativ.
  4. Am Kontaktpunkt entsteht ein **Tastpunkt mit den Regeln des Gehens** (10 cm Durchmesser, Starthelligkeit 50 %, 15 s Kurzzeitgedächtnis). Er wird erst nach der Dämpfung angelegt und ist deshalb selbst nicht gedämpft.
  5. Danach kann der Spieler sich ducken (Tasten 1–4) und das Objekt abtasten.
- **Getroffenes Körperteil:** Berührt der Körper gleichzeitig mehrere Teile, haben die Beine Vorrang (der Fuß trifft zuerst), danach Rumpf und Kopf. Der Kontaktpunkt liegt in der Draufsicht am dem Körpermittelpunkt nächsten Punkt des Objekts, in der Höhe in der Mitte des gemeinsamen Höhenbereichs. Rumpf- und Kopfkollisionen entstehen nur bei Objekten, die über den Beinen hängen (z. B. Balken, Lampen, Treppenunterseiten).

## 7. Ausrichtung und Wandführung

### 7.1 Protokoll der Tastereignisse

Jedes Tastereignis wird **in jedem Modus** (Gehen und Stehen) protokolliert mit: Objekt-ID, Fläche (Normale), Position, Zeitpunkt, Tastrichtung der Hand (vorn oder seitlich).

### 7.2 Tastpaar (Ausrichtung möglich)

Zwei Tastereignisse bilden ein gültiges **Tastpaar**, wenn sie alle Bedingungen erfüllen:

- gleiches Objekt **und gleiche Fläche** (gleiche Normale, nur senkrechte Flächen),
- Abstand der Tastpunkte 20–80 cm,
- zeitlicher Abstand **mindestens 0,4 s** und höchstens 20 s. Gleichzeitige Berührungen beider Hände zählen nicht als Paar.

Ein gültiges Tastpaar macht die Ausrichtung für 20 s **möglich** (HUD: „Ausrichtung möglich") und startet sofort die Wandführung (7.4).

### 7.3 Ausführung (manuell)

- Das Tastpaar allein dreht den Körper nie. Eine Drehung geschieht auf Tastendruck (**Leertaste**, zusätzlich Num 0) oder als automatische Parallelausrichtung beim Loslaufen (7.5).
- Die Taste wirkt nur, solange eine Ausrichtung möglich ist (Tastpaar der letzten 20 s) und keine Drehanimation läuft; sonst tut sie nichts. Nach einer manuellen Drehung kann innerhalb dieser 20 s erneut ausgerichtet werden.
- **Richtung nach der zuletzt verwendeten Tastrichtung:**
  - seitliches Tasten ergibt **parallel** zur Fläche. Von den zwei möglichen Richtungen wird die mit dem kleineren Drehwinkel gewählt.
  - Tasten nach vorn ergibt **senkrecht**, die Front zeigt zur Fläche.
- Die Drehung ist animiert (~0,35 s). Es gibt keine Entlastungspause.

### 7.4 Wandführung (Drift aus)

Mit einem gültigen Tastpaar beginnt die **Wandführung** (unabhängig davon, ob der Spieler die Ausrichtung ausführt). Beim Entlangtasten an einer Wand dient diese als Orientierungspunkt; der Körper driftet nicht ab. Die Orientierung bleibt auch nach Stillstand und Weitergehen in derselben Richtung erhalten, solange die Wandführung aktiv ist.

Die Wandführung endet erst, wenn **beide** Bedingungen gleichzeitig erfüllt sind:

1. Keine Hand hat Objektkontakt.
2. Kein Objekt befindet sich innerhalb der Handreichweite, sodass innerhalb des aktuellen Tastbandes kein neuer Kontakt entstehen kann. Richtwert: horizontaler Abstand Körpermitte zur nächsten Fläche > 1,0 m (Konstante `LOCK_RELEASE_DIST`, abgeleitet aus Schulterabstand 20 cm + Armlänge 65 cm + Suchradius 7 cm, aufgerundet).

Beim Ende der Wandführung gilt die Regel aus Abschnitt 5 (Schrittzähler auf 0). Manuelles Drehen beendet die Wandführung nicht, denn sie schaltet nur die Drift ab und erzwingt keine Blickrichtung.

### 7.5 Automatische Parallelausrichtung beim Loslaufen (v3.2)

Streckt der Spieler eine Hand seitlich aus (Maustaste links/rechts), hat diese Kontakt mit einer Wand und läuft er los, richtet sich der Körper automatisch parallel zur Wand aus.

- **Auslöser:** Beginn eines aufrechten Schritts (Vollschritt oder Halbschritt, vorwärts oder rückwärts). Im Stehen und bei Duckschritten geschieht nichts.
- **Bedingungen (alle):**
  1. Eine seitlich tastende Hand hat Kontakt mit einer **senkrechten** Fläche (Normale ±X/±Y). Oberseiten zählen nicht. Tasten nach vorn löst die Ausrichtung nicht aus.
  2. Die Blickrichtung weicht **höchstens 20°** von der Flächenrichtung ab. Gemessen wird der kleinere Winkel zu den beiden parallelen Richtungen.
  3. Es läuft keine Drehanimation (der Spieler dreht sich gerade selbst).
- **Wirkung:** Der Körper dreht sich animiert (~0,35 s) parallel zur Fläche, mit dem kleineren Drehwinkel. Die **Wandführung beginnt** (Drift aus, 7.4); die Drift des soeben begonnenen Schritts entfällt. Eine Drehung ab 1° zählt im Ausrichtungszähler des HUD wie eine manuelle Ausrichtung.
- **Über 20° Abweichung** geschieht nichts. Der Spieler verlässt die Wand offenbar schräg oder will sich von ihr lösen. Die manuelle Ausrichtung (7.3) bleibt in jedem Fall möglich.
- Die Prüfung erfolgt bei jedem Schritt, nicht nur beim ersten nach dem Stehen. Entsteht der Wandkontakt erst beim Gehen, greift die Ausrichtung also beim nächsten Schritt (spätestens nach 0,45 s). Nach der ersten Ausrichtung ist die Abweichung kleiner als 1°, so dass nichts mehr sichtbar geschieht.

## 8. Visualisierung

### 8.1 Grundprinzip

Schwarzer Bildschirm. Wird eine Stelle erfolgreich ertastet, wird **ausschließlich** ein kugelförmiger Bereich um den Tastpunkt sichtbar (Stehen: 20 cm Durchmesser, Gehen: 10 cm). Das ist eine Visualisierung des Tastvorgangs, **keine Beleuchtung**: Tasten ohne Objektkontakt erzeugt keine Visualisierung.

- Keine Lichtstrahlung; andere Objekte werden nicht erhellt.
- Benachbarte Flächen werden nicht automatisch sichtbar.
- Nur die tatsächlich ertastete Stelle wird dargestellt.
- Mehrere Visualisierungen sind gleichzeitig aktiv und können sich räumlich überlagern.
- Die Weltgeometrie selbst wird **nie** gerendert, auch Wände nicht, nur ihre ertasteten Punkte und (v3.3) die ertasteten Kantenstücke (8.5).

**Oberflächen (v3.1).** Jedes Objekt hat eine Oberflächenart. Sie wird im Muster der Kugel sichtbar; die Darstellung ist ausschließlich **weiß auf schwarz**, Farben und Graustufen tragen keine Bedeutung (Graustufen entstehen nur durch das Verblassen):

| Oberfläche | Beispiel | Muster | Langzeitdauer |
| --- | --- | --- | --- |
| Rau | Beton, Putz, Ziegel | grobe, unregelmäßige Flecken | 100 % |
| Glatt | Metall, Kunststoff | gleichmäßiger, glatter Verlauf | 100 % |
| Strukturiert | Holz, gerillte Flächen | waagerechte, leicht gewellte Streifen (Maserung) | 50 % |

Der Boden gilt als rau. Eine Kollision übernimmt die Oberflächenart des getroffenen Objekts. Das Muster wird mit der Kugel skaliert. Die Streifenrichtung ist bisher fest waagerecht; eine Ausrichtung nach der Fläche (z. B. senkrechte Maserung an Säulen) ist später möglich.

### 8.2 Hände

Zwei Ellipsoide von 20 cm Länge, entlang der Armrichtung orientiert, Fläche transparent, mit hellem Rand. Sie sind die einzigen dauerhaft sichtbaren Objekte.

### 8.3 Gedächtnis: Kurzzeit, Langzeit und Verblassungsgruppen

**Kurzzeitgedächtnis.** Jeder Tastpunkt verblasst exponentiell auf einen Restwert von **2,5 %** seiner Starthelligkeit (Spanne 2–3 %). Die Dauer hängt allein vom **Modus bei der Entstehung** ab, nicht vom späteren Bewegungszustand:

```
H(t) = H0 * M * exp(-k * t),   k = ln(1 / 0,025) / T
T = 15 s  (Punkt entstand im Gehen, auch Kollisionspunkte)
T = 30 s  (Punkt entstand im Stehen)
```

- H0 = Starthelligkeit (100 % Stehen / 50 % Gehen)
- M = Kollisionsmultiplikator (Start 1,0, Produkt der Faktoren aus Abschnitt 6)
- t = Sekunden seit dem Tastvorgang

**Langzeitgedächtnis.** Nach Ablauf von T geht der Punkt ins Langzeitgedächtnis über. Dort werden Punkte zu **Verblassungsgruppen** zusammengefasst, um die Menge der zu verwaltenden Punkte klein zu halten:

- Der Raum ist in Zellen von 25 cm Kantenlänge eingeteilt. Alle Langzeitpunkte einer Zelle bilden **eine** Gruppe (Mittelpunkt = Mittel der Mitglieder, Radius reicht bis zum entferntesten Mitglied).
- Die Gruppe beginnt mit der Helligkeit ihres hellsten Mitglieds (stetiger Übergang: keine Helligkeitssprünge beim Übergang) und verblasst **linear auf 0** innerhalb von **12 Minuten** (Mittelwert aus den 10–15 Minuten des GDD). Nimmt die Gruppe später einen weiteren Punkt auf, gelten die größere Helligkeit und das spätere Ende.
- Mit dem Verblassen wird die Darstellung **unschärfer**: der Radius wächst bis zum Verschwinden auf das Doppelte.
- Kollisionen dämpfen auch die Gruppen (Faktoren aus Abschnitt 6).
- Obergrenze der Kurzzeitpunkte: 220 (Speicherschutz). Wird sie überschritten, geht der älteste Punkt sofort ins Langzeitgedächtnis über. Bei normaler Nutzung liegt die Zahl der Kurzzeitpunkte bei etwa 120 (2 Hände, 1 Punkt je 0,5 s, 30 s).

**Offen:** Die Helligkeit von 2,5 % ist auf einem schwarzen Bildschirm kaum sichtbar. Ob die Darstellung eine Gamma-Anhebung bekommt, damit das Langzeitgedächtnis erkennbar bleibt, wird im Spieltest entschieden.

### 8.4 HUD

Kopfleiste mit Bodyzustand: Modus (Gehen/Stehen), Körperhöhe, aktive Tastpunkte, aktive Kantenstücke (v3.3), Kollisionszähler, Handkontakt L/R, Anzahl der Ausrichtungen, „Ausrichtung möglich", Status Wandführung (an/aus).

### 8.5 Kanten (v3.3)

Kanten sind wichtig, um Objekte zu erkennen (GDD Abschnitt 16: „Unterscheidung von Oberflächen über Muster und Kanten“). Eine Kugel zeigt nur, *dass* etwas berührt wurde; eine Kante zeigt die Form. Ertastete Kanten erscheinen deshalb als helle **Linien**.

- **Wann gilt eine Kante als ertastet?** Der Berührpunkt einer Hand liegt in beiden Achsen, die senkrecht zu einer Quaderkante stehen, höchstens **5 cm** von der Quaderbegrenzung entfernt (Konstante `KANTE_TOLERANZ`). An einer Ecke können das bis zu drei Kanten sein. Kanten auf Bodenhöhe (die Fußlinie eines auf dem Boden stehenden Objekts) zählen nicht, denn der Boden ist nie ertastbar (4.3). Nur Handberührungen erzeugen Kanten, Kollisionspunkte nicht.
- **Kantenstück.** Jede Kante wird ab ihrem Anfang in Stücke von **40 cm** Länge geteilt (`KANTE_LAENGE`, das letzte Stück kann kürzer sein). Sichtbar wird nur das Stück, in dem die Berührung liegt. Streicht die Hand an der Kante entlang, wachsen die Stücke zu einer durchgehenden Linie zusammen; nicht berührte Stücke bleiben unsichtbar (8.1).
- **Takt.** Anders als Tastpunkte (höchstens alle 0,5 s) werden Kanten bei **jedem Simulationsschritt** geprüft. Eine Hand, die schnell über eine Kante streicht (bis 1,6 m/s), würde die schmale Zone sonst zwischen zwei Tastpunkten verpassen.
- **Helligkeit und Gedächtnis.** Start 100 % im Stehen und **80 %** im Gehen, also heller als ein Tastpunkt (50 % im Gehen), damit Linien lesbar bleiben. Im Kurzzeitgedächtnis gilt dasselbe Verblassen wie bei Punkten (8.3: 15 s im Gehen, 30 s im Stehen bis 2,5 %). Danach sinkt die Helligkeit linear auf 0 (12 min, bei strukturierten Oberflächen 6 min). Kanten werden **nicht zu Gruppen zusammengefasst und nicht unscharf**: Ein Kantenstück bleibt eine scharfe Linie, bis es verschwindet. Kollisionen dämpfen Kanten wie Punkte (Faktoren aus Abschnitt 6).
- **Auffrischen.** Von jedem Kantenstück gibt es höchstens eine Instanz. Eine erneute Berührung setzt Helligkeit und Zeit zurück (frühestens 0,1 s nach der letzten Aufnahme, `KANTE_ERNEUERUNG`). Das ist für Kanten schon festgelegt, damit nicht bei jedem Schritt neue Objekte entstehen; ein Zeitzuschlag (+2 s, GDD Abschnitt 4) gibt es nicht. Für Tastpunkte bleibt das Auffrischen offen (15.2).
- **Darstellung (v3.4).** Die Linie ist **schwarz** und **2 Punkte breit**, unabhängig von der Entfernung (`KANTE_BREITE_PUNKTE`); sie liegt über den Tastpunkten und bildet den Kontrast zu den hellen Kugeln. Beim Verblassen verhält sie sich **gegensätzlich** zu den Kugeln: Je dunkler diese werden, desto heller wird die Linie (Grauwert bis `KANTE_AUFHELLUNG_MAX` = 60 %); unter 25 % der Starthelligkeit (`KANTE_AUSBLENDEN`) wird sie zusätzlich transparent und verschwindet am Ende des Gedächtnisses. Kein Schein, kein Saum. Linien, die teilweise hinter der Kamera liegen, werden an der Bildebene gekürzt. Auf schwarzem Grund ist eine frische Kante unsichtbar; sie wirkt nur dort, wo Tastkugeln liegen. Die Darstellung ist bewusst allgemein gehalten, damit später auch Ränder anderer Objekte so gezeichnet werden können.
- **Beispielbild:** `docs/bilder/5_kanten_pfeiler.png` zeigt die Hände am Pfeiler: zusammenhängende senkrechte Kanten, die Oberkante, und ältere, schon verblasste Stücke oben.

## 9. Testlevel (Prototyp)

Feste Welt, 12 × 10 m, Wandhöhe 2,6 m. Zwei Räume:

- **Trennwand** bei y = 0,5–0,7 mit **Durchgang** (x = −1..1).
- **Ausgang:** Lücke in der Nordwand (x = 3,5..4,5). Der Ausgang ist bewusst kein Objekt, sondern die Abwesenheit von Geometrie. Der Spieler erkennt ihn daran, dass die Hand beim Tasten entlang der Nordwand ins Leere greift.
- **Raum 1 (Süd, Start bei (−4,5, −3,5), zufällige Blickrichtung):** Regal an der Westwand (2 m hoch, strukturiert), Tisch (75 cm, strukturiert), Kiste (50 cm, glatt).
- **Raum 2 (Nord):** Sockel (75 cm, rau), Pfeiler (**1,6 m**, glatt).
- **Wände** (außen und Trennwand) sind rau.

**Ablauf bei niedrigen Objekten:** Der Spieler kollidiert zuerst mit dem Objekt (meist mit den Füßen), macht einen halben Schritt zurück und bleibt stehen. Dann setzt er die Duckmechanik ein, um das Objekt abzutasten. Die Objekthöhen erzwingen also die Duckmechanik: Wer nicht duckt, kann die niedrigen Objekte nicht abtasten.

**Erreichbarkeit nach den Tastbändern aus 4.3:**

Alle Objekte stehen auf dem Boden. Ein Objekt der Höhe H ist in einer Stufe ertastbar, wenn die Unterkante des Tastbandes unter H liegt. Tiefere Stufen erreichen also immer auch höhere Objekte; entscheidend ist die **höchste** Stufe, ab der ein Objekt erreichbar ist:

| Objekt | Höhe | Erste ertastbare Stufe (von aufrecht nach unten) |
| --- | --- | --- |
| Regal | 200 cm | Gehen und aufrecht (130–160 / 140–180), ebenso alle tieferen Stufen |
| Pfeiler | 160 cm | Gehen und aufrecht (Kontakt 130–160 bzw. 140–160), ebenso alle tieferen Stufen |
| Tisch, Sockel | 75 cm | erst ab h = 50 (60–100, Kontakt 60–75), ebenso h = 30 |
| Kiste | 50 cm | nur h = 30 (5–60, Kontakt 5–50) |

Damit erzwingen nur Tisch, Sockel und Kiste die Duckmechanik. Regal und Pfeiler werden schon im Gehen bemerkt (Handkontakt, Körper bleibt stehen).

## 10. Steuerung gesamt

| Eingabe | Aktion |
| --- | --- |
| W / S oder Pfeiltasten hoch/runter | vorwärts / rückwärts gehen; geduckt ein Schritt von 10 cm je Tastendruck |
| A / D oder Pfeiltasten links/rechts | Drehung 15° ± 1° pro Schritt; Gehtaste kurz = Halbschritt, gehalten = Vollschritte |
| 1–4 | Körperhöhe (aufrecht … geduckt) |
| Maus bewegen | Hände führen (Höhe, Spannweite) |
| keine / linke / rechte Maustaste | Tastmodus (vorn / links / rechts) |
| Leertaste (zusätzlich Num 0) | Ausrichtung ausführen (nur wenn „Ausrichtung möglich"); beim Loslaufen mit seitlichem Wandkontakt richtet der Körper sich bis 20° Abweichung automatisch parallel aus |

## 11. Offene Punkte

- **Spielziel (bewusst zurückgestellt):** Aktuell geht es um das Steuerungs- und Visualisierungskonzept. Vorschlag für später: Triggerzone etwa 1 m hinter der Lücke der Nordwand beendet das Level; Rückmeldung durch kurzes Aufhellen und Statistik (Zeit, Kollisionen, Tastpunkte). Zeitdruck zunächst weglassen.
**Entschieden (zuvor offen):** Kanten als Linien, 5 cm Toleranz, 40-cm-Stücke, jeder Schritt geprüft (8.5); Gehen nur aufrecht, geduckt Einzelschritte von 10 cm ohne Wiederholen (4.3, 5); automatische Parallelausrichtung bis 20° (7.5); tiefste Tasthöhe 5 cm, Boden nie ertastbar (4.3); Körper gestapelt, Kollisionsprüfung in 3D (Abschnitt 3); `LOCK_RELEASE_DIST` = 1,0 m (7.4); Lösen der klebenden Hand bei 10 cm Abweichung (4.4); zweistufiges Gedächtnis (8.3); Kollisionsfaktoren (6); Mehrgeschossigkeit wird eingeplant (15); Pfeiler 1,6 m (Abschnitt 9).

- **Tast-Identität:** Objekte erhalten einen Typ mit eigenem Punktmuster, z. B. Regal mit Lamellen als horizontale Streifen, Pfeiler als vertikale Punktreihe. Reine Darstellungssache, später ergänzbar.
- **Level-System:** feste Handlevel oder prozedural.
- **Audio:** später als Orientierungsebene (bewusst derzeit ausgeschlossen).

## 12. Zentrale Konstanten (Balancing)

Alle Werte liegen in einer zentralen Konstantenklasse und lassen sich ohne Eingriff in die Logik anpassen.

| Konstante | Wert |
| --- | --- |
| Tastintervall pro Hand | 0,5 s |
| Suchradius Gehen / Stehen | 7 cm / 3 cm |
| Punktdurchmesser Gehen / Stehen | 10 cm / 20 cm |
| Starthelligkeit Gehen / Stehen | 50 % / 100 % |
| Kurzzeitgedächtnis Gehen / Stehen | 15 s / 30 s |
| Restanteil am Ende des Kurzzeitgedächtnisses | 2,5 % |
| Max. Kurzzeitpunkte | 220 |
| Langzeitgedächtnis (Dauer einer Gruppe) | 12 min |
| Zelle einer Verblassungsgruppe | 25 cm (je Oberflächenart getrennt) |
| Langzeitdauer strukturierter Oberflächen | 50 % |
| Unschärfe (Radiuszuwachs bis Ende) | +100 % |
| Kollisionsfaktoren Bein / Rumpf / Kopf | 0,7 / 0,55 / 0,4 |
| Rückwärtsweg bei Kollision | 25 cm |
| Eingabesperre nach Kollision | 0,6 s |
| Schrittlänge / Gehgeschwindigkeit | 50 cm / 1,1 m/s |
| Drift: Start nach / Betrag | 10 Schritten / 1–2° |
| Drehschritt / Zufall / Dauer | 15° / ±1° / 0,35 s |
| Einzelschritt (kurzer Tastendruck) | 25 cm |
| Duckschritt (geduckt, je Tastendruck) | 10 cm |
| Gehen-Nachlauf | 0,3 s |
| Ausrichtung: Abstand der Tastpunkte | 20–80 cm |
| Ausrichtung: Zeitfenster / Gültigkeit | 20 s / 20 s |
| Ausrichtung: Mindestzeitabstand des Tastpaars | 0,4 s |
| Automatische Ausrichtung: größte Abweichung | 20° |
| `LOCK_RELEASE_DIST` | 1,0 m |
| Hand-Lösen bei klebendem Kontakt | 10 cm |
| Armlänge min / max | 25 cm / 65 cm |
| Tiefste Tasthöhe (`TAST_MIN_HOEHE`) | 5 cm |
| Kante: Toleranz (`KANTE_TOLERANZ`) | 5 cm |
| Kante: Länge eines Stücks (`KANTE_LAENGE`) | 40 cm |
| Kante: Starthelligkeit Gehen / Stehen | 80 % / 100 % |
| Kante: Mindestabstand fürs Auffrischen (`KANTE_ERNEUERUNG`) | 0,1 s |
| Kante: Linienbreite (`KANTE_BREITE_PUNKTE`) | 2 Punkte |
| Kante: größte Aufhellung / Ausblenden unter (`KANTE_AUFHELLUNG_MAX`, `KANTE_AUSBLENDEN`) | 60 % Grau / 25 % Helligkeit |
| Tastbänder | siehe 4.3 |

## 13. Auftrag für die KI / nächste Iteration

Implementiere (oder analysiere) dieses Konzept als spielbaren Prototyp in Java (Eclipse, Java 17, AWT/Swing, keine externen Abhängigkeiten, mehrere Klassen). Rahmenbedingungen:

1. Kein Licht, keine Weltgeometrie im Rendering; nur Tastpunkte, Hände und HUD.
2. Tastpunkte nur bei echter Berührung, max. alle 0,5 s pro Hand, modusabhängige Größe (10/20 cm) und Starthelligkeit (50/100 %). Anhaltender Kontakt: Hand klebt (4.4).
3. Kurzzeitgedächtnis: exponentielles Verblassen auf 2,5 % in 15 s (Punkt im Gehen) bzw. 30 s (im Stehen); danach Langzeitgedächtnis mit Verblassungsgruppen (Abschnitt 8.3).
4. Kollisionsereignis = halber Schritt (25 cm) rückwärts + Stillstand + Dämpfung aller aktiven Visualisierungen mit 0,7 / 0,55 / 0,4 (Bein / Rumpf / Kopf) + Tastpunkt am Kontaktpunkt mit den Regeln des Gehens.
5. Zufallsdrift nach 10 Schritten (1–2°, feste Seite), Drehschritte 15° ± 1°, Einzeldruck = Halbschritt, gehaltene Taste = Vollschritte. Geduckt: ein Schritt von 10 cm je Tastendruck (keine Wiederholung, keine Drift). Drift aus während der Wandführung.
6. Tastbänder nach Abschnitt 4.3 (tiefste Tasthöhe 5 cm, Boden nie ertastbar); Gehen/Stehen nach der Definition mit 0,3 s Nachlauf; Gehen nur bei Körperhöhe 90 cm.
7. Manuelle Ausrichtung nach Abschnitt 7: Protokoll in jedem Modus, Tastpaar (gleiche Fläche, 20–80 cm, 0,4–20 s), Ausführung nur per Leertaste/Num 0, Parallel (seitliches Tasten) oder Senkrecht (Tasten nach vorn), Wandführung ab gültigem Tastpaar bis kein Objekt mehr in Reichweite. Zusätzlich automatische Parallelausrichtung beim Loslaufen mit seitlichem Wandkontakt bis 20° Abweichung (Abschnitt 7.5).
8. Hände synchron gespiegelt, im Körper-Koordinatensystem geführt (drehen immer mit), synchroner Stopp bei einseitigem Kontakt, definierte Startpose.
9. Alle Balancing-Werte in einer zentralen Konstantenklasse (Abschnitt 12).
10. Kanten nach Abschnitt 8.5: Berührung höchstens 5 cm von einer Quaderkante ertastet ein 40-cm-Kantenstück, Prüfung bei jedem Simulationsschritt, Darstellung als schwarze 2-Punkt-Linie über den Tastpunkten, beim Verblassen aufgehellt.

## 14. Umsetzungsentscheidungen des Eclipse-Prototyps (v2.1)

Beim Umsetzen mussten einige Details festgelegt werden, die das Konzept offenließ. Sie sind im Code in `Konstanten` bzw. den genannten Klassen nachvollziehbar und leicht änderbar.

| Thema | Entscheidung |
| --- | --- |
| Armlänge | Wird horizontal gemessen (nach vorn ab Torsovorderkante bzw. seitlich ab Schulter), unabhängig von der Handhöhe. Das ergibt eine Handreichweite von 0,80 m nach vorn. Bei Körperkontakt (Beine 0,40 m) bleiben genau 25 cm Mindestarmlänge. |
| Vorwärtstasten | Beide Hände strecken sich mit 1,6 m/s auf volle Länge. Maus links/rechts bestimmt die Spannweite (10–50 cm je Hand), Maus hoch/runter die Höhe im Tastband. |
| Seitliches Tasten | Der seitlich tastende Arm zeigt 60° von der Vorwärtsrichtung nach außen, damit die Hand im Sichtfeld (120°) bleibt. Der andere Arm bleibt vorn. |
| Mausbereich | Die mittleren 60 % des Fensters decken den ganzen Bewegungsbereich ab (höhere Empfindlichkeit); der Mauszeiger ist ausgeblendet. |
| Hand klebt | Löst sich, wenn die Zielposition um 10 cm seitlich oder von der Fläche weg abweicht. Danach gleitet die Hand an der Fläche entlang zum Ziel und bleibt dort wieder kleben. Bewegung in das Objekt löst nicht. |
| Halten | Eine klebende Hand verliert den Kontakt, wenn ihr Abstand zur Fläche größer als 8 cm wird. |
| Handstopp beim Gehen | Nach einem Stopp durch die vordere Hand muss die Gehtaste losgelassen und neu gedrückt werden, sonst läuft der Körper sofort gegen das Hindernis. |
| Eingabesperre | Nach einer Kollision sind Gehen und Drehen 0,6 s gesperrt, Ducken (1–4) und Handführung nicht. |
| Rückstoß | Der halbe Schritt (25 cm) erfolgt entgegen der Bewegungsrichtung, animiert mit Gehgeschwindigkeit. |
| Drift | Wird pro Schritt anteilig als Drehung während des Schritts angewendet. Vorwärts- und Rückwärtsschritte zählen. |
| Ausrichtung | Nur senkrechte Flächen (Normale ±X/±Y). Beim Ausführen mit weniger als 1° Drehung wird nicht gezählt. Die Wandführung beginnt schon mit dem gültigen Tastpaar. |
| Tastabstand | Der Abstand der Tastpunkte für die Ausrichtung ist der 3D-Abstand. |
| Automatische Ausrichtung (v3.2) | Wird beim Beginn jedes aufrechten Schritts geprüft (Körper meldet den Schrittbeginn, das Spiel ruft `Ausrichtung.beiSchrittBeginn`). Sie schaltet die Wandführung ein, verwirft die Drift des begonnenen Schritts und zählt im selben Zähler wie die manuelle Ausrichtung. |
| Duckschritte (v3.2) | Jeder Tastendruck ergibt genau einen Schritt von 10 cm; nach dem Schritt sperrt die Gehsperre bis zum Loslassen der Taste. Kein Halbschritt, keine Drift, Modus „Stehen“. Die Blockade durch die vordere Hand gilt unverändert. |
| Tastband und Boden (v3.2) | Das tiefste Band beginnt bei 5 cm. Die Hände sondieren ohne Boden (`Welt.sondiere(..., false)`); die Variante mit Boden bleibt für die Welt und die Tests erhalten. |
| Kanten (v3.3) | `Welt.kanten` liefert zu einer `Beruehrung` die `Kantenstueck`e (Record, Schlüssel der Verwaltung); `Tastkante` hält Zeit und Helligkeit, `Tastpunkte` verwaltet sie in einer Map (ein Eintrag je Stück). `Spiel` prüft bei jedem Schritt jede Hand mit Kontakt. Die Geometrie liegt in `welt`, damit `tasten` weiterhin nur `welt` nutzt (keine Zyklen). |
| Kantenlinie (v3.4) | `Ansicht.zeichneKanten`: eine schwarze Linie von 2 Punkten, nach den Punkten gezeichnet, Grauwert und Deckkraft aus der relativen Helligkeit; die Kamera kürzt Strecken an der Bildebene (`Kamera.strecke`). |
| Wandführung | Endet, wenn keine Hand Kontakt hat und der horizontale Abstand zur nächsten Fläche im aktuellen Tastband größer als 1,0 m ist. |
| Kamera | Auge in der Kopfmitte (Körperhöhe + 70 cm), Sichtfeld 120°, Blick fest an die Körperausrichtung gebunden. |
| Entwicklungshilfe | F3 blendet eine Draufsicht ein (nur zum Testen, widerspricht dem Blind-Konzept). F1 blendet die Hilfe ein, R startet neu. |

## 15. Mehrgeschossigkeit (Planung) und Abgleich mit dem Blindspace-GDD

### 15.1 Mehrgeschossigkeit

Gebäude mit mehreren Etagen sind Ziel des Spiels (GDD Abschnitte 2, 3 und 10). Der Prototyp hat heute **einen** Boden bei z = 0. Damit sich Treppen später ohne Umbau der Steuerung ergänzen lassen, gilt für die Weiterentwicklung:

- **Boden als Teil der Welt:** Der virtuelle Boden (ID −1) wird durch Geschossplatten ersetzt, die wie Quader aufgebaut sind (mit Oberkante = Bodenhöhe der Etage). Die Welt beantwortet die Frage „Wie hoch ist der begehbare Boden an Stelle (x, y)?".
- **Treppen** werden aus Stufenquadern (Stufenhöhe etwa 18 cm, Auftritt etwa 28 cm) aufgebaut, nicht aus Schrägen. Damit bleibt die achsenparallele Quader-Welt erhalten, Tastproben und Wandführung funktionieren unverändert.
- **Körperhöhe über Grund:** Die Körperposition erhält eine Fußhöhe z. Der Körper steigt eine Stufe, wenn sie nicht höher als die Schrittstufe (etwa 25 cm) ist; höhere Kanten sind Hindernisse (Beinkollision). Beinzylinder, Oberkörper und Kopf sitzen weiterhin gestapelt auf der Fußhöhe. Die Kamera folgt der Fußhöhe weich.
- **Tastbänder** gelten relativ zur Fußhöhe (Hände sind am Körper befestigt).
- **Stufen ertasten (v3.2):** Mit dem tiefsten Band ab 5 cm sind Stufen (18 cm, 36 cm, ...) in der Körperhöhe 30 cm ertastbar (Band 5–60 cm); höhere Stufen auch in den höheren Stufen, sobald das Band unter der Stufenkante beginnt. Die Duckschritte von 10 cm (Abschnitt 5) helfen, sich einer Stufe zu nähern.
- **Decken und Treppenunterseiten** sind schwebende Quader (`Welt.addSchwebend` existiert bereits). Dort sind Rumpf- und Kopfkollisionen möglich (Abschnitt 6).
- **Kollisionspunkte und Tastpunkte** sind bereits dreidimensional und brauchen keine Änderung; die Raumzellen der Verblassungsgruppen sind ebenfalls 3D.
- **Wandführung:** `naechsterAbstand` und die Tastbänder gelten je Etage (Quader außerhalb des Tastbands zählen nicht).
- **Level-Schnittstelle:** Ein Level beschreibt künftig mehrere Etagen mit Verbindungen (Treppe, Aufzug nach Bedarf).

**Bereits vorbereitet (v3.1):** Jeder Quader trägt eine Objektkategorie (`Objektart`: u. a. Tür, Griff, Schalter, Gegenstand, Treppe, Geländer als Platzhalter ohne Verhalten) und eine Oberflächenart. Berührungen und Kollisionen liefern beides mit, und `Welt.objekt(id)` führt von einer Berührung zum Objekt. Interaktionen können damit später am Objekttyp ansetzen, ohne die Tastlogik umzubauen.

**Noch nicht vorbereitet:** Fußhöhe des Körpers und Bodenhöhe der Welt (die Handhöhen sind heute absolute Weltkoordinaten; sie müssten relativ zur Fußhöhe werden), veränderliche Objekte (Tür auf/zu, Schalter, aufgenommene Gegenstände; `Quader` ist unveränderlich) und eine Interaktionstaste. Diese Teile sind ohne eine konkrete Treppe bzw. Tür nicht sinnvoll testbar und werden mit dem jeweiligen Thema umgesetzt.

Eine Umsetzung ist für Phase 3 des GDD vorgesehen und erfordert die Änderungen in `Welt` (Bodenhöhe), `Koerper` (Fußhöhe, Stufen) und `Ansicht` (Kameraführung).

### 15.2 Abgleich mit dem Blindspace-GDD (Version 1.0)

| Thema | GDD | Entscheidung / Stand |
| --- | --- | --- |
| Kollisionsfaktoren | Bein 0,7 / Rumpf 0,55 / Kopf 0,4 | übernommen (Abschnitt 6) |
| Kollisionspunkt | Kugel 10 cm am Kontaktpunkt | übernommen: Regeln des Gehens (Abschnitt 6) |
| Verblassen | Stillstand ~30 s, Bewegung ~15 s, Rest 2–5 % | übernommen: nach Modus bei der Entstehung, 2,5 % (8.3) |
| Langzeitgedächtnis | 10–15 min, zunehmend unscharf | übernommen: 12 min, Verblassungsgruppen, Unschärfe (8.3) |
| „Strukturen verblassen schneller" | Strukturen verblassen im Langzeitgedächtnis schneller | übernommen für die Oberflächenart „strukturiert" (50 % der Langzeitdauer); das GDD lässt offen, ob damit Oberflächen oder komplexe Objekte gemeint sind |
| Auffrischen | Erneutes Berühren setzt die Helligkeit zurück, +2 s, höchstens 6 s | **offen für Tastpunkte:** im Prototyp entsteht bei jedem Tasten ein neuer Punkt (kein Zurücksetzen). **Für Kanten (v3.3)** gilt das Zurücksetzen schon, ohne Zeitzuschlag (8.5) |
| Oberflächen | Rau / glatt / strukturiert, Schwarz-Weiß-Muster | übernommen (8.1) |
| Interaktion | Türen, Griffe, Schlüssel, Schalter | offen: veränderliche Welt und Interaktionstaste nötig |
| Mehrgeschossigkeit | Treppen, Podeste | geplant (15.1) |
| Audio | später, unterstützend | später; Prototyp bewusst ohne Audio |
| Tastpunkt-Menge | – | durch Verblassungsgruppen begrenzt |
| Kanten | „Unterscheidung von Oberflächen über Muster und Kanten“ (Abschnitt 16); Kontakte erzeugen kugelförmige Visualisierungen (Abschnitt 14) | **umgesetzt (v3.3, 8.5). Abweichung:** Kantenstücke sind Linien statt Kugeln, 40 cm lang, schwarz statt hell (Kontrast zu den Kugeln), mit eigener Helligkeit (80 % im Gehen) und ohne Gruppierung im Langzeitgedächtnis. Das GDD beschreibt Kanten nur in der Zusammenfassung; Form und Werte sind eine Auslegung und im Spieltest zu prüfen |
| Automatische Ausrichtung, Duckschritte, Tastband ab 5 cm | – (nicht im GDD) | konzeptinterne Ergänzungen (v3.2), kein Widerspruch zum GDD; GDD Abschnitt 14 nennt Bewegungsungenauigkeit als Schwierigkeitsfaktor, die Wandführung mildert sie an Wänden |
| Darstellung | nur Schwarz/Weiß | übernommen für die Tastpunkte (weiß auf schwarz). Die Handränder (hell, bei Kontakt orange) und das HUD sind Rückmeldungen des Prototyps und bleiben farbig |
