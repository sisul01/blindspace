package blindspace.test;

import static blindspace.konfig.Konstanten.*;

import blindspace.anzeige.Ansicht;
import blindspace.levels.Level;
import blindspace.spiel.Spiel;
import blindspace.spieler.Ausrichtung;
import blindspace.spieler.Haende;
import blindspace.spieler.Koerper;
import blindspace.tasten.Tastkante;
import blindspace.tasten.Tastpunkt;
import blindspace.tasten.Tastpunkte;
import blindspace.welt.Beruehrung;
import blindspace.welt.Kantenstueck;
import blindspace.welt.Koerperteil;
import blindspace.welt.Objektart;
import blindspace.welt.Oberflaeche;
import blindspace.welt.Quader;
import blindspace.welt.Welt;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Headless-Selbsttest der Kernregeln des Konzepts (ohne JUnit, ohne Fenster).
 * Start: Rechtsklick auf die Klasse -> Run As -> Java Application.
 * Beendet mit Exit-Code 1, wenn ein Test fehlschlägt.
 */
public final class Selbsttest {
    /** Anzahl bestandener Prüfungen. */
    private static int ok;

    /** Anzahl fehlgeschlagener Prüfungen. */
    private static int fehler;

    /** Testklasse, keine Instanzen. */
    private Selbsttest() {
    }

    /**
     * Führt alle Tests aus und beendet mit Exit-Code 1, wenn einer fehlschlägt.
     *
     * @param args Kommandozeilenargumente (nicht ausgewertet)
     */
    public static void main(String[] args) {
        verblassen();
        startpose();
        gehenBisWand();
        kollisionDannDuckenUndTasten();
        duckschritte();
        tastbandBisBoden();
        koerperteile();
        oberflaechen();
        einzelUndVollschritt();
        zufallsStart();
        handLoesenUndGleiten();
        seitlichesTastenStopptNichtUndRichtetAus();
        autoAusrichtungGrenzen();
        kantenGeometrie();
        kantenGedaechtnis();
        kantenImSpiel();
        kantenDarstellung();
        ausrichtungSenkrechtUndParallel();
        ausrichtungNurMitMoeglichkeit();
        ausrichtungKriterien();
        wandfuehrungEnde();
        drift();
        System.out.println();
        System.out.println(ok + " Pruefungen bestanden, " + fehler + " fehlgeschlagen.");
        if (fehler > 0) {
            System.exit(1);
        }
    }

    // ===== Hilfen =====
    /**
     * Wertet eine Bedingung aus, zählt sie und gibt das Ergebnis aus.
     *
     * @param bedingung Ergebnis der Prüfung
     * @param text      Beschreibung der Prüfung für die Ausgabe
     */
    static void pruefe(boolean bedingung, String text) {
        if (bedingung) {
            ok++;
            System.out.println("  ok      " + text);
        } else {
            fehler++;
            System.out.println("  FEHLER  " + text);
        }
    }

    /**
     * Lässt das Spiel eine bestimmte Zeit in festen Simulationsschritten laufen.
     *
     * @param s        Spiel
     * @param sekunden Dauer in Sekunden
     */
    static void lauf(Spiel s, double sekunden) {
        int n = (int) Math.round(sekunden / SIM_DT);
        for (int i = 0; i < n; i++) {
            s.schritt(SIM_DT);
        }
    }

    /**
     * Sucht einen Quader der Welt anhand seines Namens.
     *
     * @param s    Spiel
     * @param name Name des Quaders
     * @return der gefundene Quader
     * @throws IllegalStateException wenn kein Quader mit diesem Namen existiert
     */
    static Quader finde(Spiel s, String name) {
        for (Quader q : s.welt.quader()) {
            if (q.name.equals(name)) {
                return q;
            }
        }
        throw new IllegalStateException(name);
    }

    /**
     * Gibt eine Überschrift für eine Testgruppe aus.
     *
     * @param t Überschrift
     */
    static void titel(String t) {
        System.out.println(t);
    }

    // ===== Tests =====
    /**
     * Prüft das Gedächtnismodell: Kurzzeit 15 s/30 s auf 2,5 %, Langzeitgruppen, Überlauf und Kollisionsfaktoren.
     */
    static void verblassen() {
        titel("Gedächtnis: Kurzzeit 15 s / 30 s auf 2,5 %, Langzeitgruppen, Kollisionsfaktoren (Abschnitt 8)");
        Tastpunkt g = new Tastpunkt(0, 0, 0, 0, 0.5, 0.1, KZ_DAUER_GEHEN, 1);
        pruefe(Math.abs(g.helligkeit(15) - 0.5 * KZ_REST) < 1e-9, "Gehen: nach 15 s 2,5 % der Starthelligkeit");
        Tastpunkt st = new Tastpunkt(0, 0, 0, 0, 1.0, 0.2, KZ_DAUER_STEHEN, 1);
        pruefe(Math.abs(st.helligkeit(30) - KZ_REST) < 1e-9, "Stehen: nach 30 s 2,5 %");
        pruefe(st.helligkeit(15) > 0.15, "Stehen: nach 15 s noch deutlich sichtbar (" + st.helligkeit(15) + ")");

        Tastpunkte t = new Tastpunkte();
        t.hinzufuegen(g);
        t.aufraeumen(14.9);
        pruefe(t.anzahl() == 1 && t.anzahlGruppen() == 0, "bei 14,9 s noch Kurzzeitpunkt");
        t.aufraeumen(15.0);
        pruefe(t.anzahl() == 0 && t.anzahlGruppen() == 1, "nach 15 s im Langzeitgedächtnis (Gruppe)");
        double h0 = t.gruppen().iterator().next().helligkeit(15.0);
        pruefe(Math.abs(h0 - 0.5 * KZ_REST) < 1e-9, "Gruppe beginnt mit dem Restwert (stetiger Übergang)");
        double hMitte = t.gruppen().iterator().next().helligkeit(15.0 + LZ_DAUER / 2);
        pruefe(Math.abs(hMitte - h0 / 2) < 1e-9, "Gruppe verblasst linear");
        t.aufraeumen(15.0 + LZ_DAUER + 0.1);
        pruefe(t.anzahlGruppen() == 0, "nach der Langzeitdauer (12 min) gelöscht");

        // Zusammenfassen: 50 Punkte in einer Zelle -> eine Gruppe
        Tastpunkte z = new Tastpunkte();
        for (int i = 0; i < 50; i++) {
            z.hinzufuegen(new Tastpunkt(0.05 + i * 0.002, 0.05, 0.05, i * 0.1, 0.5, 0.1, KZ_DAUER_GEHEN, 1));
        }
        z.aufraeumen(60.0);
        pruefe(z.anzahl() == 0 && z.anzahlGruppen() == 1 && z.gruppen().iterator().next().anzahl() == 50,
                "50 Punkte derselben Raumzelle: eine Verblassungsgruppe");
        z.hinzufuegen(new Tastpunkt(5, 5, 1, 60.0, 0.5, 0.1, KZ_DAUER_GEHEN, 1));
        z.aufraeumen(80.0);
        pruefe(z.anzahlGruppen() == 2, "entfernter Punkt bildet eine eigene Gruppe");

        // Überlauf: ältester Punkt geht direkt ins Langzeitgedächtnis
        Tastpunkte u = new Tastpunkte();
        for (int i = 0; i <= MAX_TASTPUNKTE; i++) {
            u.hinzufuegen(new Tastpunkt(i, 0, 0, 0, 0.5, 0.1, KZ_DAUER_GEHEN, 1));
        }
        pruefe(u.anzahl() == MAX_TASTPUNKTE && u.anzahlGruppen() == 1, "Überlauf: ältester Punkt wird zur Gruppe");

        // Kollisionsfaktoren wirken auf Kurzzeitpunkte und Gruppen, kumulativ
        Tastpunkte k = new Tastpunkte();
        Tastpunkt q = new Tastpunkt(0, 0, 0, 0, 1.0, 0.2, KZ_DAUER_STEHEN, 1);
        k.hinzufuegen(q);
        k.hinzufuegen(new Tastpunkt(9, 9, 1, -100, 1.0, 0.2, KZ_DAUER_STEHEN, 2));
        k.aufraeumen(0.0);
        double gh = k.gruppen().iterator().next().helligkeit(0.0);
        k.kollision(0.7, 0.0);
        pruefe(Math.abs(q.helligkeit(0) - 0.7) < 1e-9, "Beine: Kurzzeitpunkt × 0,7");
        pruefe(Math.abs(k.gruppen().iterator().next().helligkeit(0.0) - gh * 0.7) < 1e-9, "Beine: Gruppe × 0,7");
        k.kollision(0.55, 0.0);
        k.kollision(0.4, 0.0);
        pruefe(Math.abs(q.helligkeit(0) - 0.7 * 0.55 * 0.4) < 1e-9, "Faktoren wirken kumulativ");
        pruefe(Koerperteil.BEIN.faktor() == 0.7 && Koerperteil.RUMPF.faktor() == 0.55 && Koerperteil.KOPF.faktor() == 0.4,
                "Faktoren Bein 0,7 / Rumpf 0,55 / Kopf 0,4");
    }

    /**
     * Prüft die Startpose der Hände (spiegelbildlich, Brusthöhe, halb ausgestreckt).
     */
    static void startpose() {
        titel("Startpose (Abschnitt 4.1)");
        Spiel s = new Spiel(1);
        pruefe(s.haende.links.bx == -START_SPANN && s.haende.rechts.bx == START_SPANN, "Hände spiegelbildlich");
        pruefe(Math.abs(s.haende.links.bz - 1.45) < 1e-9, "Brusthöhe z = 145 cm");
        pruefe(Math.abs(s.haende.links.by - (SCHULTER_Y + 0.45)) < 1e-9, "halb ausgestreckt");
    }

    /**
     * Prüft das Gehen bis zur Wand: Handstopp ohne Kollision, Stopp an der Handreichweite, Tastintervall, Kontakt beim Abtasten, Gehsperre.
     */
    static void gehenBisWand() {
        titel("Gehen bis zur Wand: vordere Hand stoppt den Körper (Abschnitt 4.5)");
        Spiel s = new Spiel(2);
        s.eingabe.mausBewegt = true;
        s.eingabe.vor = true;
        boolean bandOk = true;
        for (int i = 0; i < (int) (12 / SIM_DT); i++) {
            s.schritt(SIM_DT);
            if (s.koerper.gehen() && s.koerper.stufe() == 0) {
                for (Haende.Hand h : s.haende.alle()) {
                    if (h.bz < 1.30 - 1e-9 && Math.abs(h.bz - 1.45) > 1e-6 && h.bz > 1.0) {
                        // Übergang vom Start (1.45) ist erlaubt, darunter nicht
                        bandOk &= h.bz >= 1.30 - 0.15;
                    }
                }
            }
        }
        double y = s.koerper.y;
        pruefe(s.koerper.kollisionen() == 0, "keine Körperkollision");
        pruefe(y < 0.5 - BEIN_RADIUS, "Körper bleibt vor der Trennwand (y = " + String.format("%.2f", y) + ")");
        // Handreichweite: 0,15 (Torsovorderkante) + 0,65 (Arm) + 0,07 (Suchradius) = 0,87 m -> Stopp bei y = -0,37
        pruefe(Math.abs(y - (0.5 - SCHULTER_Y - ARM_MAX - SUCHRADIUS_GEHEN)) < 0.03,
                "Stopp bei Handreichweite (0,87 m vor der Wand)");
        pruefe(s.tastpunkte.anzahl() > 0, "Tastpunkte entstanden");
        boolean walkPunkt = false;
        for (Tastpunkt p : s.tastpunkte.alle()) {
            walkPunkt |= p.durchmesser == PUNKT_DURCHMESSER_GEHEN || p.durchmesser == PUNKT_DURCHMESSER_STEHEN;
        }
        pruefe(walkPunkt, "Punktgröße 10 bzw. 20 cm");
        double y1 = s.koerper.y;
        // Nach dem Gehstopp (Hand 7 cm von der Wand) bleibt der Kontakt beim Führen der Hände erhalten
        s.eingabe.vor = false;
        s.eingabe.mausU = 0.9;
        s.eingabe.mausV = 0.8;
        lauf(s, 1.5);
        pruefe(s.haende.links.kontakt != null && s.haende.rechts.kontakt != null,
                "Hände behalten beim Abtasten der Wand den Kontakt (Spannweite 46 cm, Höhe 172 cm)");
        s.eingabe.vor = true;
        lauf(s, 0.1);
        s.eingabe.vor = false;
        lauf(s, 0.1);
        pruefe(Math.abs(s.koerper.y - y1) < 0.02, "erneutes Gehen gegen die Wand: Körper bleibt stehen");
        s.eingabe.vor = true;
        lauf(s, 0.1);
        lauf(s, 2.0);
        pruefe(Math.abs(s.koerper.y - y1) < 1e-9, "bei gehaltener Taste kein erneutes Anlaufen (Gehtaste neu drücken)");
        int maxPunkte = 2 * (1 + (int) Math.ceil(14.0 / TASTINTERVALL));
        pruefe(s.tastpunkte.anzahl() <= maxPunkte, "höchstens ein Punkt je Hand und 0,5 s");
        s.eingabe.vor = false;
        lauf(s, 0.1);
        s.eingabe.rueck = true;
        lauf(s, 1.2);
        pruefe(s.koerper.y < y1 - 0.4, "nach Loslassen ist Rückwärtsgehen möglich");
    }

    /**
     * Prüft Kollision mit der Kiste, Dämpfung der Tastpunkte, halben Schritt zurück, Ducken und Abtasten.
     */
    static void kollisionDannDuckenUndTasten() {
        titel("Kollision mit niedrigem Objekt, halber Schritt zurück, Ducken, Abtasten (Abschnitt 6, 9)");
        Spiel s = new Spiel(3);
        Quader kiste = finde(s, "Kiste");
        s.koerper.x = 3.25;
        s.koerper.y = -2.0;
        s.koerper.kurs = Math.PI; // Blick nach Süden
        s.eingabe.mausBewegt = true;
        s.eingabe.mausU = 0.2;
        s.eingabe.mausV = 0.5;
        // Alter Tastpunkt zum Prüfen der Dämpfung
        Tastpunkt alt = new Tastpunkt(0, 0, 0, s.zeit(), 1.0, 0.2, KZ_DAUER_STEHEN, 99);
        s.tastpunkte.hinzufuegen(alt);
        s.eingabe.vor = true;
        double kx = 0, ky = 0;
        for (int i = 0; i < (int) (6 / SIM_DT) && s.koerper.kollisionen() == 0; i++) {
            s.schritt(SIM_DT);
        }
        pruefe(s.koerper.kollisionen() == 1, "Kollision mit der Kiste (Hand war über der Kiste)");
        pruefe(Math.abs(alt.mult - Koerperteil.BEIN.faktor()) < 1e-9, "Beine: alle Visualisierungen × 0,7");
        boolean kollPunkt = false;
        for (Tastpunkt p : s.tastpunkte.alle()) {
            kollPunkt |= p.objektId == kiste.id && p.durchmesser == PUNKT_DURCHMESSER_GEHEN && p.h0 == HELLIGKEIT_GEHEN
                    && p.kurzdauer == KZ_DAUER_GEHEN && p.mult == 1.0;
        }
        pruefe(kollPunkt, "Kollision erzeugt einen Tastpunkt mit den Regeln des Gehens (10 cm, 50 %, 15 s), nicht gedämpft");
        kx = s.koerper.x;
        ky = s.koerper.y;
        s.eingabe.vor = false;
        lauf(s, 1.0);
        double rueck = Math.hypot(s.koerper.x - kx, s.koerper.y - ky);
        pruefe(Math.abs(rueck - RUECKSTOSS) < 0.03, "halber Schritt zurück (" + String.format("%.3f", rueck) + " m)");
        pruefe(s.koerper.sperre() == 0, "Eingabesperre ist abgelaufen");

        // Ducken auf Stufe 4 (h = 30 cm) und Kiste abtasten
        s.eingabe.stufeWunsch = 3;
        s.eingabe.mausV = 0.5;
        lauf(s, 3.0);
        boolean kisteGetastet = false;
        for (Tastpunkt p : s.tastpunkte.alle()) {
            kisteGetastet |= p.objektId == kiste.id;
        }
        pruefe(s.koerper.stufe() == 3, "Körperstufe 4");
        pruefe(kisteGetastet, "Kiste wurde im Hocken ertastet (halber Schritt reicht für die Handreichweite)");
        // Geduckt: Einzelschritte von 10 cm vorwärts (die Hände liegen auf der Kiste) und rückwärts
        double vx = s.koerper.x, vy = s.koerper.y;
        s.eingabe.vor = true;
        lauf(s, 1.0);
        double vor = Math.hypot(s.koerper.x - vx, s.koerper.y - vy);
        pruefe(Math.abs(vor - DUCK_SCHRITT) < 0.005 && s.koerper.kollisionen() == 1,
                "geduckt: vorwärts ein Einzelschritt von 10 cm (" + String.format("%.3f", vor) + " m), keine Kollision");
        s.eingabe.vor = false;
        lauf(s, 0.1);
        double rx = s.koerper.x, ry = s.koerper.y;
        s.eingabe.rueck = true;
        lauf(s, 1.0);
        double weg = Math.hypot(s.koerper.x - rx, s.koerper.y - ry);
        pruefe(Math.abs(weg - DUCK_SCHRITT) < 0.005, "geduckt: rückwärts ein Einzelschritt von 10 cm ("
                + String.format("%.3f", weg) + " m)");
    }

    /**
     * Prüft die Einzelschritte von 10 cm in geduckter Haltung (vorwärts und rückwärts, kein Wiederholen bei
     * gehaltener Taste, kein Gehen-Modus, keine Drift) und dass Drehen in jeder Körperstufe möglich bleibt.
     */
    static void duckschritte() {
        titel("Geduckt: Einzelschritte von 10 cm, Drehen bleibt möglich (Abschnitte 4.3, 5)");
        Spiel s = new Spiel(4);
        s.eingabe.stufeWunsch = 1;
        lauf(s, 0.5);
        double y0 = s.koerper.y;
        s.eingabe.vor = true;
        lauf(s, 0.03);
        pruefe(s.koerper.schrittAktiv() && !s.koerper.gehen(), "Duckschritt läuft und gilt als Stehen (nicht als Gehen)");
        lauf(s, 1.5);
        pruefe(Math.abs(s.koerper.y - y0 - DUCK_SCHRITT) < 0.005, "Stufe 70 cm, Taste gehalten: genau ein Schritt von 10 cm ("
                + String.format("%.3f", s.koerper.y - y0) + " m)");
        pruefe(s.koerper.schrittZaehler() == 0, "Duckschritte zählen nicht für die Drift");
        s.eingabe.vor = false;
        lauf(s, 0.1);
        s.eingabe.vor = true;
        lauf(s, 0.5);
        pruefe(Math.abs(s.koerper.y - y0 - 2 * DUCK_SCHRITT) < 0.005, "erneuter Tastendruck: weiterer Schritt von 10 cm");
        s.eingabe.vor = false;
        lauf(s, 0.1);
        s.eingabe.rueck = true;
        lauf(s, 0.5);
        pruefe(Math.abs(s.koerper.y - y0 - DUCK_SCHRITT) < 0.005, "rückwärts: Schritt von 10 cm");
        s.eingabe.rueck = false;
        lauf(s, 0.1);
        s.eingabe.stufeWunsch = 3;
        lauf(s, 0.5);
        double y1 = s.koerper.y;
        s.eingabe.rueck = true;
        lauf(s, 1.5);
        pruefe(s.koerper.stufe() == 3 && Math.abs(y1 - s.koerper.y - DUCK_SCHRITT) < 0.005,
                "Stufe 30 cm: ebenfalls 10 cm, gehaltene Taste wiederholt nicht");
        s.eingabe.rueck = false;
        lauf(s, 0.1);
        s.eingabe.stufeWunsch = 0;
        lauf(s, 0.5);
        double y2 = s.koerper.y;
        s.eingabe.vor = true;
        lauf(s, 0.05);
        s.eingabe.vor = false;
        lauf(s, 1.0);
        pruefe(Math.abs(s.koerper.y - y2 - HALBSCHRITT) < 0.01, "aufrecht bleibt der Einzeldruck ein Halbschritt (25 cm)");
        // Vordere Hand an der Ostwand: Der Duckschritt vorwärts ist blockiert, rückwärts möglich
        Spiel w = new Spiel(43);
        w.koerper.x = 5.3;
        w.koerper.y = -4.0;
        w.koerper.kurs = -Math.PI / 2; // Blick nach Osten zur Wand
        w.eingabe.stufeWunsch = 1;
        w.eingabe.mausBewegt = true;
        w.eingabe.mausV = 0.5;
        lauf(w, 1.5);
        pruefe(w.haende.links.kontakt != null && w.haende.rechts.kontakt != null, "Aufbau: beide Hände berühren die Wand");
        double wx = w.koerper.x;
        w.eingabe.vor = true;
        lauf(w, 1.0);
        pruefe(Math.abs(w.koerper.x - wx) < 1e-9, "geduckt: die vordere Hand an der Wand blockiert den Schritt vorwärts");
        w.eingabe.vor = false;
        lauf(w, 0.1);
        w.eingabe.rueck = true;
        lauf(w, 1.0);
        pruefe(Math.abs(wx - w.koerper.x - DUCK_SCHRITT) < 0.005, "geduckt: rückwärts von der Wand weg ein Schritt von 10 cm");
        s.eingabe.stufeWunsch = 1;
        lauf(s, 0.3);
        s.eingabe.links = true;
        lauf(s, 0.1);
        s.eingabe.links = false;
        lauf(s, 0.5);
        double d = Math.abs(Koerper.normalisiere(s.koerper.kurs));
        pruefe(Math.abs(d - Math.toRadians(15)) <= Math.toRadians(1) + 1e-9,
                "Drehen geduckt möglich: " + String.format("%.1f", Math.toDegrees(d)) + "°");
    }

    /**
     * Erzeugt ein Level mit zwei Stufen (18 und 36 cm) ohne Außenwände, Start bei (0 | 0,15), Blick nach Norden.
     *
     * @return Level mit zwei Stufenquadern "Stufe 1" (ID 1) und "Stufe 2" (ID 2)
     */
    static Level stufenLevel() {
        return new Level() {
            @Override
            public String name() {
                return "Stufen";
            }

            @Override
            public Welt erzeuge() {
                Welt w = new Welt();
                w.add("Stufe 1", Objektart.TREPPE, Oberflaeche.RAU, -1.0, 0.9, 1.0, 1.2, 0.18);
                w.add("Stufe 2", Objektart.TREPPE, Oberflaeche.RAU, -1.0, 1.2, 1.0, 1.5, 0.36);
                return w;
            }

            @Override
            public double startX() {
                return 0;
            }

            @Override
            public double startY() {
                return 0.15;
            }

            @Override
            public double startKurs() {
                return 0;
            }
        };
    }

    /**
     * Prüft das Tastband bis knapp über den Boden: Stufen sind geduckt ertastbar, der Boden und flache
     * Gegenstände nicht, das Maximum bleibt bei 180 cm.
     */
    static void tastbandBisBoden() {
        titel("Tastband bis knapp über dem Boden: Stufen ertastbar, Boden nicht (Abschnitt 4.3)");
        pruefe(Math.abs(bandUnten(false, 3) - TAST_MIN_HOEHE) < 1e-12 && TAST_MIN_HOEHE > 0 && TAST_MIN_HOEHE <= 0.10,
                "tiefstes Band beginnt knapp über dem Boden (" + Math.round(TAST_MIN_HOEHE * 100) + " cm)");
        pruefe(Math.abs(bandOben(false, 0) - 1.80) < 1e-12, "Maximum bleibt 180 cm");
        pruefe(bandOben(false, 3) == 0.60 && bandUnten(false, 2) == 0.60 && bandUnten(false, 0) == 1.40,
                "übrige Bänder unverändert");

        Welt w = new Welt();
        w.add("Teppich", -1, 0, 1, 1, 0.01);
        pruefe(w.sondiere(0, 0.5, TAST_MIN_HOEHE, SUCHRADIUS_STEHEN, false) == null,
                "flacher Gegenstand (1 cm) liegt unter dem Tastband: nicht ertastbar");
        pruefe(w.sondiere(0, 5.0, TAST_MIN_HOEHE, SUCHRADIUS_STEHEN, false) == null,
                "Boden wird von den Händen nie ertastet");
        Beruehrung boden = w.sondiere(0, 5.0, TAST_MIN_HOEHE, 0.1);
        pruefe(boden != null && boden.objektId() == Welt.BODEN_ID, "die Tastprobe der Welt kennt den Boden weiterhin");

        Spiel s = new Spiel(40, stufenLevel());
        s.eingabe.stufeWunsch = 3;
        s.eingabe.mausBewegt = true;
        s.eingabe.mausU = 0.5;
        s.eingabe.mausV = 0.2; // z = 5 + 0,2 * 55 = 16 cm
        lauf(s, 2.0);
        boolean stufe = false;
        for (Tastpunkt p : s.tastpunkte.alle()) {
            stufe |= p.objektId == 1;
        }
        pruefe(stufe && s.koerper.kollisionen() == 0, "Stufe (18 cm) wird geduckt (30 cm) ertastet");
        Spiel a = new Spiel(41, stufenLevel());
        a.eingabe.mausBewegt = true;
        a.eingabe.mausV = 0.5;
        lauf(a, 2.0);
        pruefe(a.tastpunkte.anzahl() == 0, "aufrecht: Stufe unterhalb des Tastbands, kein Kontakt");
        Spiel b = new Spiel(42, stufenLevel());
        b.eingabe.stufeWunsch = 3;
        b.eingabe.mausBewegt = true;
        b.eingabe.mausV = 0.0; // tiefste Handhöhe
        b.koerper.y = -2.0;
        lauf(b, 2.0);
        pruefe(b.tastpunkte.anzahl() == 0 && b.haende.links.kontakt == null && b.haende.rechts.kontakt == null,
                "tiefste Handhöhe ohne Objekt: auch nach 2 s kein Kontakt mit dem Boden");
    }

    /**
     * Prüft das Kleben der Hand, das Lösen ab 10 cm Abweichung und das Gleiten zum neuen Ziel.
     */
    static void handLoesenUndGleiten() {
        titel("Hand klebt, löst sich bei 10 cm, gleitet zum Ziel (Abschnitt 4.4)");
        Spiel s = new Spiel(5);
        s.koerper.x = -4.5;
        s.koerper.y = -0.2;
        s.eingabe.mausBewegt = true;
        s.eingabe.mausU = 0.375; // Spannweite 25 cm
        s.eingabe.mausV = 0.125;
        lauf(s, 1.5);
        Haende.Hand l = s.haende.links;
        pruefe(l.haelt && s.haende.rechts.haelt, "beide Hände kleben an der Trennwand");
        double x0 = l.bx;
        s.eingabe.mausU = 0.375 + 0.09 / 0.4;
        lauf(s, 0.5);
        pruefe(l.haelt && Math.abs(l.bx - x0) < 1e-9, "9 cm Abweichung: Hand bleibt kleben");
        s.eingabe.mausU = 0.375 + 0.11 / 0.4;
        lauf(s, 1.0);
        pruefe(Math.abs(l.bx - (-(SPANN_MIN + s.eingabe.mausU * (SPANN_MAX - SPANN_MIN)))) < 0.01 && l.haelt,
                "11 cm Abweichung: Hand gleitet zum Ziel und klebt dort wieder (bx = " + String.format("%.3f", l.bx)
                        + ")");
        pruefe(l.kontakt != null && l.kontakt.objektId() == finde(s, "Trennwand West").id, "Kontakt bleibt an der Wand");
    }

    /**
     * Prüft, dass seitlicher Handkontakt den Körper nicht stoppt, beim Loslaufen automatisch parallel
     * ausgerichtet wird (Abweichung höchstens 20 Grad) und die Wandführung beginnt.
     */
    static void seitlichesTastenStopptNichtUndRichtetAus() {
        titel("Seitliches Tasten im Gehen: Körper läuft weiter und richtet beim Loslaufen automatisch parallel aus (Abschnitte 4.5, 7.5)");
        Spiel s = new Spiel(6);
        s.koerper.x = 5.3;
        s.koerper.y = -4.0;
        s.koerper.kurs = -0.10; // leicht zur Ostwand
        s.eingabe.mausBewegt = true;
        s.eingabe.mausRechts = true;
        s.eingabe.mausV = 0.5;
        s.eingabe.vor = true;
        lauf(s, 1.0);
        double yMitte = s.koerper.y;
        pruefe(s.haende.rechts.haelt, "rechte Hand klebt an der Ostwand");
        pruefe(yMitte > -3.6, "Körper läuft trotz seitlichem Kontakt weiter (y = " + String.format("%.2f", yMitte) + ")");
        pruefe(s.koerper.kollisionen() == 0, "keine Kollision");
        s.eingabe.vor = false;
        lauf(s, 1.5);
        pruefe(s.ausrichtung.moeglich(), "Ausrichtung möglich (gültiges Tastpaar)");
        pruefe(s.ausrichtung.wandfuehrung(), "Wandführung aktiv");
        pruefe(s.ausrichtung.anzahl() == 1, "genau eine automatische Ausrichtung beim Loslaufen");
        pruefe(Math.abs(Koerper.normalisiere(s.koerper.kurs)) < Math.toRadians(1.0),
                "parallel zur Wand (Blick nach Norden): " + String.format("%.2f", Math.toDegrees(s.koerper.kurs)) + "°");
        s.eingabe.ausrichten = true;
        lauf(s, 1.0);
        pruefe(s.ausrichtung.anzahl() == 1, "Leertaste danach: Drehung unter 1 Grad, nicht erneut gezählt");
    }

    /**
     * Erzeugt einen Spiel-Aufbau mit der rechten Hand (seitlich tastend) an der Ostwand des Testlevels.
     *
     * @param seed       Startwert des Zufallsgenerators
     * @param kurs       Startblickrichtung in Radiant
     * @param stufe      gewünschte Körperstufe 0 bis 3
     * @param mausV      Handhöhe im Tastband (0 bis 1)
     * @param y          Start-Y-Position (x ist 5,3)
     * @return das Spiel nach 1 s Stillstand; die Hand klebt dann an der Wand
     */
    static Spiel seitenhandAnOstwand(long seed, double kurs, int stufe, double mausV, double y) {
        Spiel s = new Spiel(seed);
        s.koerper.x = 5.3;
        s.koerper.y = y;
        s.koerper.kurs = kurs;
        s.eingabe.stufeWunsch = stufe;
        s.eingabe.mausBewegt = true;
        s.eingabe.mausRechts = true;
        s.eingabe.mausV = mausV;
        lauf(s, 1.0);
        return s;
    }

    /**
     * Prüft die automatische Parallelausrichtung: nur beim Loslaufen, nur bis 20 Grad Abweichung, nur mit
     * seitlichem Kontakt an einer senkrechten Fläche, auch rückwärts, nicht bei Duckschritten und nicht während
     * einer Drehung.
     */
    static void autoAusrichtungGrenzen() {
        titel("Automatische Parallelausrichtung: Bedingungen und Grenzen (Abschnitt 7.5)");
        Spiel a = seitenhandAnOstwand(50, -0.30, 0, 0.5, -4.0); // 17,2° zur Wand
        pruefe(a.haende.rechts.haelt, "Aufbau: rechte Hand klebt an der Ostwand");
        pruefe(a.ausrichtung.anzahl() == 0 && Math.abs(a.koerper.kurs + 0.30) < 1e-9,
                "im Stehen (ohne Loslaufen) keine automatische Ausrichtung");
        a.eingabe.vor = true;
        lauf(a, 1.0);
        pruefe(Math.abs(Koerper.normalisiere(a.koerper.kurs)) < Math.toRadians(1.0) && a.ausrichtung.anzahl() == 1
                && a.ausrichtung.wandfuehrung(), "Loslaufen bei 17°: parallel ausgerichtet, Wandführung an");

        Spiel b = seitenhandAnOstwand(51, -0.44, 0, 0.5, -4.0); // 25,2° zur Wand
        b.eingabe.vor = true;
        lauf(b, 0.1);
        b.eingabe.vor = false;
        lauf(b, 0.8);
        pruefe(b.ausrichtung.anzahl() == 0 && Math.abs(b.koerper.kurs + 0.44) < 1e-9 && b.koerper.kollisionen() == 0,
                "Loslaufen bei 25°: keine automatische Ausrichtung, Blickrichtung unverändert");

        Spiel r = seitenhandAnOstwand(52, -0.30, 0, 0.5, -2.0);
        double yr = r.koerper.y;
        r.eingabe.rueck = true;
        lauf(r, 1.0);
        pruefe(Math.abs(Koerper.normalisiere(r.koerper.kurs)) < Math.toRadians(1.0) && r.koerper.y < yr - 0.5
                && r.ausrichtung.anzahl() == 1, "auch beim Rückwärtsgehen: parallel ausgerichtet");

        Spiel d = seitenhandAnOstwand(53, -0.30, 1, 0.5, -4.0);
        pruefe(d.haende.rechts.haelt && d.koerper.stufe() == 1, "Aufbau geduckt (70 cm): Hand klebt an der Wand");
        d.eingabe.vor = true;
        lauf(d, 1.0);
        pruefe(d.ausrichtung.anzahl() == 0 && Math.abs(d.koerper.kurs + 0.30) < 1e-9,
                "Duckschritte richten nicht automatisch aus");

        // Grenzwerte direkt an der Ausrichtung, mit künstlichem Handkontakt an einer Wand mit Normale -X
        Beruehrung wand = new Beruehrung(5, 0, -1, 6.0, 0.0, 1.5, 0.0, Oberflaeche.RAU, Objektart.WAND);
        Spiel g1 = new Spiel(54, leeresLevel());
        g1.haende.rechts.seitlich = true;
        g1.haende.rechts.kontakt = wand;
        g1.koerper.kurs = Math.toRadians(19.5);
        pruefe(g1.ausrichtung.beiSchrittBeginn(g1.koerper, g1.haende) && g1.koerper.drehAktiv(),
                "19,5° Abweichung: Ausrichtung beginnt");
        lauf(g1, 0.5);
        pruefe(Math.abs(g1.koerper.kurs) < 1e-6, "Ziel: Wandrichtung Norden");

        Spiel g2 = new Spiel(55, leeresLevel());
        g2.haende.rechts.seitlich = true;
        g2.haende.rechts.kontakt = wand;
        g2.koerper.kurs = Math.toRadians(20.5);
        pruefe(!g2.ausrichtung.beiSchrittBeginn(g2.koerper, g2.haende) && !g2.koerper.drehAktiv()
                && !g2.ausrichtung.wandfuehrung() && g2.ausrichtung.anzahl() == 0, "20,5° Abweichung: nichts geschieht");

        Spiel g3 = new Spiel(56, leeresLevel());
        g3.haende.rechts.seitlich = true;
        g3.haende.rechts.kontakt = wand;
        g3.koerper.kurs = Math.PI - 0.2; // fast nach Süden: der kleinere Drehwinkel führt nach Süden
        g3.ausrichtung.beiSchrittBeginn(g3.koerper, g3.haende);
        lauf(g3, 0.5);
        pruefe(Math.abs(Math.abs(g3.koerper.kurs) - Math.PI) < 1e-6, "kleinerer Drehwinkel: Ausrichtung nach Süden");

        Spiel g4 = new Spiel(57, leeresLevel());
        g4.haende.rechts.seitlich = false; // Hand tastet nach vorn
        g4.haende.rechts.kontakt = wand;
        g4.koerper.kurs = 0.1;
        pruefe(!g4.ausrichtung.beiSchrittBeginn(g4.koerper, g4.haende), "Tasten nach vorn richtet nicht automatisch aus");

        Spiel g5 = new Spiel(58, leeresLevel());
        g5.haende.rechts.seitlich = true;
        g5.haende.rechts.kontakt = new Beruehrung(5, 2, 1, 6.0, 0.0, 0.5, 0.0, Oberflaeche.RAU, Objektart.SONSTIGES);
        g5.koerper.kurs = 0.1;
        pruefe(!g5.ausrichtung.beiSchrittBeginn(g5.koerper, g5.haende), "Oberseiten werden nicht ausgerichtet");

        Spiel g6 = new Spiel(59, leeresLevel());
        g6.haende.rechts.seitlich = true;
        g6.haende.rechts.kontakt = wand;
        g6.koerper.kurs = 0.1;
        g6.koerper.drehe(0.2);
        pruefe(!g6.ausrichtung.beiSchrittBeginn(g6.koerper, g6.haende), "während einer Drehung keine automatische Ausrichtung");
    }

    /**
     * Prüft Oberflächenarten und Objektkategorien in Welt, Berührung, Tastpunkt und Gruppen.
     */
    static void oberflaechen() {
        titel("Oberflächen: rau, glatt, strukturiert (GDD Abschnitt 3)");
        Spiel s = new Spiel(30);
        Welt w = s.welt;
        Beruehrung regal = w.sondiere(-5.55, -2.0, 1.0, 0.05);
        pruefe(regal != null && regal.oberflaeche() == Oberflaeche.STRUKTURIERT && regal.art() == Objektart.MOEBEL,
                "Regal: strukturiert (Holz), Möbel");
        Beruehrung wand = w.sondiere(-6.05, 0.0, 1.0, 0.1);
        pruefe(wand != null && wand.oberflaeche() == Oberflaeche.RAU && wand.art() == Objektart.WAND,
                "Westwand: rau (Beton), Wand");
        Beruehrung kiste = w.sondiere(3.25, -3.45, 0.55, 0.1);
        pruefe(kiste != null && kiste.oberflaeche() == Oberflaeche.GLATT, "Kiste: glatt");
        Beruehrung boden = w.sondiere(0, -4, 0.05, 0.1);
        pruefe(boden != null && boden.objektId() == Welt.BODEN_ID && boden.art() == Objektart.BODEN, "Boden erkannt");
        pruefe(w.objekt(finde(s, "Pfeiler").id).art == Objektart.SAEULE && w.objekt(Welt.BODEN_ID) == null,
                "Objektsuche über die ID");

        Welt k = new Welt();
        k.add("Saeule", Objektart.SAEULE, Oberflaeche.RAU, 0.3, -0.5, 0.5, 0.5, 2.0);
        Welt.Kollision stoss = k.koerperKollision(0, 0, 0, 0);
        pruefe(stoss != null && stoss.oberflaeche() == Oberflaeche.RAU, "Kollision kennt die Oberflächenart");

        Tastpunkte t = new Tastpunkte();
        t.hinzufuegen(new Tastpunkt(0.1, 0.1, 0.1, 0, 0.5, 0.1, KZ_DAUER_GEHEN, 1, Oberflaeche.RAU));
        t.hinzufuegen(new Tastpunkt(0.12, 0.1, 0.1, 0, 0.5, 0.1, KZ_DAUER_GEHEN, 1, Oberflaeche.STRUKTURIERT));
        t.aufraeumen(20.0);
        pruefe(t.anzahlGruppen() == 2, "gleiche Zelle, verschiedene Oberflächenarten: getrennte Gruppen");
        double halb = LZ_DAUER * Oberflaeche.STRUKTURIERT.langzeitFaktor();
        for (var g : t.gruppen()) {
            double dauer = g.oberflaeche() == Oberflaeche.STRUKTURIERT ? halb : LZ_DAUER;
            pruefe(g.helligkeit(15.0 + dauer + 0.01) == 0, g.oberflaeche() + ": Langzeitdauer " + Math.round(dauer) + " s");
        }
        pruefe(Oberflaeche.STRUKTURIERT.langzeitFaktor() < Oberflaeche.RAU.langzeitFaktor(),
                "Strukturen verblassen im Langzeitgedächtnis schneller");

        Spiel g = new Spiel(31);
        g.koerper.x = -4.3;
        g.koerper.y = -2.0;
        g.koerper.kurs = Math.PI / 2; // Blick nach Westen zum Regal
        g.eingabe.mausBewegt = true;
        g.eingabe.mausU = 0.5;
        g.eingabe.mausV = 0.5;
        g.eingabe.vor = true;
        lauf(g, 4.0);
        boolean regalPunkt = false;
        for (Tastpunkt p : g.tastpunkte.alle()) {
            regalPunkt |= p.objektId == finde(g, "Regal").id && p.oberflaeche == Oberflaeche.STRUKTURIERT;
        }
        pruefe(regalPunkt, "Tastpunkte am Regal tragen die Oberflächenart STRUKTURIERT");
    }

    /**
     * Prüft, dass die Kollision das getroffene Körperteil und den Kontaktpunkt liefert.
     */
    static void koerperteile() {
        titel("Kollision: Körperteil und Kontaktpunkt (Abschnitt 6)");
        Welt w = new Welt();
        w.add("Kiste", 0.3, -0.5, 0.5, 0.5, 0.8);
        Welt.Kollision b = w.koerperKollision(0, 0, 0, 0);
        pruefe(b != null && b.teil() == Koerperteil.BEIN, "niedriges Objekt: Beine");
        pruefe(b != null && Math.abs(b.px() - 0.3) < 1e-9 && Math.abs(b.py()) < 1e-9 && Math.abs(b.pz() - 0.4) < 1e-9,
                "Kontaktpunkt an der Objektkante, in der Mitte des Überlapps");
        Welt r = new Welt();
        r.addSchwebend("Balken", 0.1, -0.5, 1.0, 0.5, 0.5, 1.3);
        Welt.Kollision rk = r.koerperKollision(0, 0, 0, 0);
        pruefe(rk != null && rk.teil() == Koerperteil.RUMPF, "Hängeobjekt auf Brusthöhe: Rumpf");
        Welt h = new Welt();
        h.addSchwebend("Lampe", -0.05, -0.5, 1.55, 0.5, 0.5, 1.7);
        Welt.Kollision hk = h.koerperKollision(0, 0, 0, 0);
        pruefe(hk != null && hk.teil() == Koerperteil.KOPF, "Hängeobjekt auf Kopfhöhe: Kopf");
        pruefe(h.koerperKollision(0, 0, 0, 3) == null, "geduckt (30 cm) ist das Hängeobjekt kein Hindernis");
    }

    /**
     * Prüft Halbschritt bei kurzem Tastendruck und kontinuierliche Vollschritte bei gehaltener Taste.
     */
    static void einzelUndVollschritt() {
        titel("Kurzer Druck = Halbschritt, langer Druck = Vollschritte bis zum Loslassen (Abschnitt 5)");
        Spiel a = new Spiel(20, leeresLevel());
        a.eingabe.vor = true;
        lauf(a, 0.05);
        a.eingabe.vor = false;
        lauf(a, 1.0);
        pruefe(Math.abs(a.koerper.y - HALBSCHRITT) < 0.01, "Einzeldruck: " + String.format("%.2f", a.koerper.y) + " m");
        Spiel r = new Spiel(21, leeresLevel());
        r.eingabe.rueck = true;
        lauf(r, 0.05);
        r.eingabe.rueck = false;
        lauf(r, 1.0);
        pruefe(Math.abs(r.koerper.y + HALBSCHRITT) < 0.01, "Einzeldruck rückwärts: Halbschritt");
        Spiel l = new Spiel(22, leeresLevel());
        l.eingabe.vor = true;
        lauf(l, 2.0);
        l.eingabe.vor = false;
        lauf(l, 1.0);
        double n = l.koerper.y / SCHRITTLAENGE;
        pruefe(Math.abs(n - Math.round(n)) < 0.02 && n >= 4, "langer Druck: ganze Schritte (" + String.format("%.2f", n) + ")");
        Spiel m = new Spiel(23, leeresLevel());
        m.eingabe.vor = true;
        lauf(m, 0.35);
        m.eingabe.vor = false;
        lauf(m, 1.0);
        pruefe(Math.abs(m.koerper.y - SCHRITTLAENGE) < 0.01, "Loslassen nach dem halben Schritt: Schritt wird beendet");
    }

    /**
     * Prüft die zufällige Startrichtung des normalen Spiels und die feste Richtung bei reproduzierbarem Start.
     */
    static void zufallsStart() {
        titel("Startrichtung zufällig (nur normales Spiel)");
        java.util.Set<Long> kurse = new java.util.HashSet<>();
        for (int i = 0; i < 20; i++) {
            Spiel s = new Spiel();
            kurse.add(Math.round(s.koerper.kurs * 1000));
            s.reset();
            kurse.add(Math.round(s.koerper.kurs * 1000));
        }
        pruefe(kurse.size() > 30, "Startrichtung streut bei Start und Neustart (" + kurse.size() + " verschiedene Werte)");
        pruefe(new Spiel(1).koerper.kurs == new Spiel(2).koerper.kurs, "Spiel mit Seed: feste Richtung des Levels");
    }

    /**
     * Verschiebt den Körper stetig (1 cm je Simulationsschritt) in Y-Richtung, damit die Hände unterwegs Kontakt
     * bekommen wie beim Gehen und nicht in ein Objekt hineinspringen.
     *
     * @param s    Spiel
     * @param ziel Ziel-Y-Koordinate des Körpers
     */
    static void geheY(Spiel s, double ziel) {
        while (Math.abs(ziel - s.koerper.y) > 1e-9) {
            s.koerper.y += Math.max(-0.01, Math.min(0.01, ziel - s.koerper.y));
            s.schritt(SIM_DT);
        }
    }

    /**
     * Erzeugt ein Kantenstück für Tests (senkrechte Kante an der Ecke eines Quaders mit ID 1).
     *
     * @param oberflaeche Oberflächenart des Quaders
     * @return ein Kantenstück bei (0 | 2) von z = 1,0 bis 1,2
     */
    static Kantenstueck teststueck(Oberflaeche oberflaeche) {
        return new Kantenstueck(1, 2, 0, 0, 2, 0, 2.0, 1.0, 0, 2.0, 1.2, oberflaeche);
    }

    /**
     * Prüft, welche Kantenstücke eine Berührung ertastet: nur nahe einer Kante (5 cm), Raster ab dem
     * Kantenanfang, Ecken mit mehreren Kanten, keine Bodenkanten.
     */
    static void kantenGeometrie() {
        titel("Kanten: Welche Berührungen ertasten eine Kante? (Abschnitt 8.5)");
        Welt w = new Welt();
        w.add("Pfeiler", Objektart.SAEULE, Oberflaeche.GLATT, 1.0, 2.0, 1.4, 2.4, 1.6);
        List<Kantenstueck> k;

        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.0, 1.0, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.size() == 1 && k.get(0).achse() == 2, "senkrechte Vorderkante: genau eine Kante, Richtung Z");
        pruefe(k.get(0).zelle() == 2 && Math.abs(k.get(0).z0() - 0.8) < 1e-9 && Math.abs(k.get(0).z1() - 1.2) < 1e-9
                && k.get(0).x0() == 1.0 && k.get(0).y0() == 2.0 && k.get(0).x1() == 1.0 && k.get(0).y1() == 2.0,
                "Stück 2 (z 0,8 bis 1,2) auf der Kante bei (1,0 | 2,0)");
        pruefe(k.get(0).seiteA() == 0 && k.get(0).seiteB() == 0 && k.get(0).objektId() == 1, "Lage der Kante und Objekt-ID");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.2, 2.0, 1.0, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.isEmpty(), "Flächenmitte (20 cm von den Kanten): keine Kante");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.049, 1.0, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.size() == 1, "4,9 cm von der Kante entfernt: ertastet");
        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.06, 1.0, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.isEmpty(), "6 cm von der Kante entfernt: nicht ertastet");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.2, 2.0, 1.58, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.size() == 1 && k.get(0).achse() == 0 && k.get(0).z0() == 1.6 && k.get(0).z1() == 1.6
                && k.get(0).x0() == 1.0 && Math.abs(k.get(0).x1() - 1.4) < 1e-9, "Oberkante: Kante in X-Richtung bei z = 1,6");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.0, 1.6, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.size() == 3 && k.stream().map(Kantenstueck::achse).distinct().count() == 3, "Ecke oben: drei Kanten (X, Y, Z)");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.0, 0.03, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        pruefe(k.size() == 1 && k.get(0).achse() == 2, "Ecke am Boden: nur die senkrechte Kante, keine Bodenkanten");

        k = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.0, 1.599, 0.0, Oberflaeche.GLATT, Objektart.SAEULE), KANTE_TOLERANZ,
                KANTE_LAENGE);
        Kantenstueck senkrecht = k.stream().filter(x -> x.achse() == 2).findFirst().orElse(null);
        pruefe(senkrecht != null && senkrecht.zelle() == 3 && Math.abs(senkrecht.z0() - 1.2) < 1e-9
                && Math.abs(senkrecht.z1() - 1.6) < 1e-9, "letztes Stück endet an der Quaderkante (1,2 bis 1,6)");

        Kantenstueck s1 = w.kanten(new Beruehrung(1, 1, -1, 1.0, 2.0, 0.9, 0.0, Oberflaeche.GLATT, Objektart.SAEULE),
                KANTE_TOLERANZ, KANTE_LAENGE).get(0);
        Kantenstueck s2 = w.kanten(new Beruehrung(1, 0, -1, 1.0, 2.02, 1.1, 0.0, Oberflaeche.GLATT, Objektart.SAEULE),
                KANTE_TOLERANZ, KANTE_LAENGE).get(0);
        pruefe(s1.equals(s2), "zwei Berührungen im selben Stück liefern dasselbe Kantenstück");

        pruefe(w.kanten(new Beruehrung(Welt.BODEN_ID, 2, 1, 1.0, 2.0, 0.0, 0.0, Oberflaeche.GLATT, Objektart.SONSTIGES),
                KANTE_TOLERANZ, KANTE_LAENGE).isEmpty(), "der Boden hat keine Kanten");
        pruefe(w.kanten(new Beruehrung(99, 0, 1, 1.0, 2.0, 1.0, 0.0, Oberflaeche.GLATT, Objektart.SONSTIGES),
                KANTE_TOLERANZ, KANTE_LAENGE).isEmpty(), "unbekannte Objekt-ID: keine Kante");
    }

    /**
     * Prüft das Gedächtnis der Kanten: Helligkeitsverlauf in zwei Phasen ohne Sprung, Auffrischen, Dämpfung durch
     * Kollisionen und Löschen.
     */
    static void kantenGedaechtnis() {
        titel("Kanten: Verblassen, Auffrischen, Kollision (Abschnitte 8.2, 8.5)");
        Tastkante k = new Tastkante(teststueck(Oberflaeche.GLATT), 10.0, KANTE_HELLIGKEIT_STEHEN, KZ_DAUER_STEHEN);
        pruefe(Math.abs(k.helligkeit(10.0) - KANTE_HELLIGKEIT_STEHEN) < 1e-12, "Starthelligkeit im Stehen 100 %");
        pruefe(KANTE_HELLIGKEIT_GEHEN > HELLIGKEIT_GEHEN && KANTE_HELLIGKEIT_STEHEN >= HELLIGKEIT_STEHEN,
                "Kanten sind im Gehen heller als Tastpunkte, im Stehen mindestens so hell");
        pruefe(Math.abs(k.helligkeit(10.0 + KZ_DAUER_STEHEN) - KZ_REST) < 1e-9, "nach 30 s im Stehen Restanteil von 2,5 %");
        double davor = k.helligkeit(10.0 + KZ_DAUER_STEHEN - 1e-6), danach = k.helligkeit(10.0 + KZ_DAUER_STEHEN + 1e-6);
        pruefe(Math.abs(davor - danach) < 1e-6, "kein Sprung beim Übergang ins Langzeitgedächtnis");
        double halb = k.helligkeit(10.0 + KZ_DAUER_STEHEN + LZ_DAUER / 2);
        pruefe(Math.abs(halb - KZ_REST / 2) < 1e-6, "Langzeitphase: linear, nach der halben Dauer der halbe Rest");
        pruefe(!k.abgelaufen(10.0 + KZ_DAUER_STEHEN + LZ_DAUER - 1) && k.abgelaufen(10.0 + KZ_DAUER_STEHEN + LZ_DAUER),
                "Kante verschwindet nach Kurz- plus Langzeitdauer");
        Tastkante str = new Tastkante(teststueck(Oberflaeche.STRUKTURIERT), 0, 1.0, KZ_DAUER_STEHEN);
        pruefe(!str.abgelaufen(KZ_DAUER_STEHEN + LZ_DAUER * LZ_FAKTOR_STRUKTURIERT - 1)
                && str.abgelaufen(KZ_DAUER_STEHEN + LZ_DAUER * LZ_FAKTOR_STRUKTURIERT), "Kanten strukturierter Objekte verblassen schneller");

        Tastpunkte t = new Tastpunkte();
        Kantenstueck st = teststueck(Oberflaeche.GLATT);
        pruefe(t.kante(new Tastkante(st, 0, 1.0, KZ_DAUER_STEHEN)) && t.anzahlKanten() == 1, "neues Kantenstück aufgenommen");
        pruefe(!t.kante(new Tastkante(st, KANTE_ERNEUERUNG / 2, 1.0, KZ_DAUER_STEHEN)) && t.anzahlKanten() == 1,
                "Berührung im selben Stück innerhalb von 0,1 s: nichts Neues");
        pruefe(t.kante(new Tastkante(st, 20.0, 1.0, KZ_DAUER_STEHEN)) && t.anzahlKanten() == 1
                && t.kanten().iterator().next().t0 == 20.0, "erneute Berührung später: aufgefrischt, weiterhin ein Stück");
        Kantenstueck anderes = new Kantenstueck(1, 2, 1, 0, 2, 1.4, 2.0, 1.0, 1.4, 2.0, 1.2, Oberflaeche.GLATT);
        t.kante(new Tastkante(anderes, 20.0, 1.0, KZ_DAUER_STEHEN));
        pruefe(t.anzahlKanten() == 2, "anderes Stück: eigener Eintrag");
        double vor = t.kanten().iterator().next().helligkeit(21.0);
        t.kollision(0.7, 21.0);
        double nach = t.kanten().iterator().next().helligkeit(21.0);
        pruefe(Math.abs(nach - 0.7 * vor) < 1e-12, "Kollision dämpft auch Kanten (hier Beine: 0,7)");
        t.aufraeumen(20.0 + KZ_DAUER_STEHEN + LZ_DAUER + 1);
        pruefe(t.anzahlKanten() == 0, "abgelaufene Kanten werden beim Aufräumen entfernt");
        t.kante(new Tastkante(st, 0, 1.0, KZ_DAUER_STEHEN));
        t.leeren();
        pruefe(t.anzahlKanten() == 0, "leeren entfernt auch Kanten");
    }

    /**
     * Prüft die Kantenerzeugung im Spiel: Hände treffen beim Herantreten die Pfeilerkante, Flächenmitten ergeben
     * keine Kante, Kanten entstehen bei jedem Schritt (nicht nur im 0,5-s-Takt der Tastpunkte), Neustart löscht.
     */
    static void kantenImSpiel() {
        titel("Kanten im Spiel: Hand trifft Kante beim Herantreten (Abschnitt 8.5)");
        // Pfeiler im Testlevel: x 1,0 bis 1,4, y 2,0 bis 2,4, Höhe 1,6; Hände bei u = 0,25 genau an den Kanten
        Spiel s = new Spiel(60);
        s.koerper.x = 1.2;
        s.koerper.y = 0.9;
        s.koerper.kurs = 0;
        s.eingabe.mausBewegt = true;
        s.eingabe.mausU = 0.25;
        s.eingabe.mausV = 0.05; // z = 1,42
        lauf(s, 0.6);
        for (Haende.Hand h : s.haende.alle()) {
            h.tastTimer = 100; // Tastpunkte gesperrt: Kanten dürfen nicht vom 0,5-s-Takt abhängen
        }
        geheY(s, 1.3);
        pruefe(s.tastpunkte.anzahl() == 0, "Aufbau: noch kein Tastpunkt (Takt gesperrt)");
        int senkrechte = 0;
        for (Tastkante k : s.tastpunkte.kanten()) {
            if (k.stueck.achse() == 2 && k.stueck.objektId() == 12) {
                senkrechte++;
            }
        }
        pruefe(senkrechte == 2, "beide senkrechten Vorderkanten ertastet, unabhängig vom Tastpunkt-Takt (" + senkrechte + ")");
        double hell = 0;
        for (Tastkante k : s.tastpunkte.kanten()) {
            hell = Math.max(hell, k.helligkeit(s.zeit()));
        }
        pruefe(hell > 0.9, "frische Kante fast mit voller Helligkeit (" + String.format("%.2f", hell) + ")");

        Spiel m = new Spiel(61);
        m.koerper.x = 1.2;
        m.koerper.y = 0.9;
        m.koerper.kurs = 0;
        m.eingabe.mausBewegt = true;
        m.eingabe.mausU = 0.0; // Hände bei x = 1,1 und 1,3: je 10 cm von den Kanten
        m.eingabe.mausV = 0.3; // z = 1,52: 8 cm unter der Oberkante
        lauf(m, 0.6);
        geheY(m, 1.3);
        lauf(m, 1.0);
        pruefe(m.tastpunkte.anzahl() > 0 && m.tastpunkte.anzahlKanten() == 0,
                "Flächenmitte berührt: Tastpunkte, aber keine Kante");

        s.reset();
        pruefe(s.tastpunkte.anzahlKanten() == 0, "Neustart löscht die Kanten");
    }

    /**
     * Prüft die Darstellung der Kanten: helle Linie am erwarteten Bildpunkt, schwarz daneben, dunkler bei
     * verblasster Kante, keine Ausgabe (und kein Fehler) für Kanten hinter der Kamera, Kürzen an der Bildebene.
     */
    static void kantenDarstellung() {
        titel("Kanten: Darstellung als helle Linien (Abschnitt 8.5)");
        int w = 1280, h = 720;
        Spiel s = new Spiel(62, leeresLevel());
        double ez = s.koerper.augenZ();
        double f = (w / 2.0) / Math.tan(SICHTFELD / 2);
        int px = w / 2, py = (int) Math.round(h / 2.0 - f * (1.1 - ez) / 2.0); // Mitte der Kante bei y = 2 m, z = 1,1
        pruefe(py > 40 && py < h - 40, "Aufbau: erwarteter Bildpunkt (" + px + "|" + py + ") liegt im Bild");

        BufferedImage leer = zeichne(s, w, h);
        pruefe(helligkeit(leer, px, py) < 10, "ohne Kante bleibt der Bildpunkt schwarz");

        s.tastpunkte.kante(new Tastkante(new Kantenstueck(1, 2, 0, 0, 2, 0, 2.0, 1.0, 0, 2.0, 1.2, Oberflaeche.GLATT),
                s.zeit(), 1.0, KZ_DAUER_STEHEN));
        BufferedImage mit = zeichne(s, w, h);
        pruefe(helligkeit(mit, px, py) > 240, "frische Kante: weißer Kern (" + helligkeit(mit, px, py) + ")");
        pruefe(helligkeit(mit, px + 80, py) < 10 && helligkeit(mit, px, py - 150) < 10, "neben und über der Linie bleibt es schwarz");
        int breite = 0;
        for (int x = px - 40; x <= px + 40; x++) {
            if (helligkeit(mit, x, py) > 200) {
                breite++;
            }
        }
        pruefe(breite >= (int) KANTE_MIN_PIXEL - 1, "Linie mindestens " + (int) KANTE_MIN_PIXEL + " Pixel breit (" + breite + ")");

        Spiel alt = new Spiel(63, leeresLevel());
        alt.tastpunkte.kante(new Tastkante(teststueck(Oberflaeche.GLATT), alt.zeit() - 29.0, 1.0, KZ_DAUER_STEHEN));
        pruefe(helligkeit(zeichne(alt, w, h), px, py) < helligkeit(mit, px, py) / 2, "verblasste Kante ist deutlich dunkler");

        // Kanten außerhalb des Blickfelds: hinter der Kamera und an ihr vorbei
        Spiel hinten = new Spiel(64, leeresLevel());
        hinten.tastpunkte.kante(new Tastkante(new Kantenstueck(1, 2, 0, 0, 0, 0, -2.0, 1.0, 0, -2.0, 1.2, Oberflaeche.GLATT),
                hinten.zeit(), 1.0, KZ_DAUER_STEHEN));
        pruefe(helligkeit(zeichne(hinten, w, h), px, py) < 10, "Kante hinter der Kamera wird nicht gezeichnet");

        Spiel quer = new Spiel(65, leeresLevel());
        quer.tastpunkte.kante(new Tastkante(new Kantenstueck(1, 1, 0, 0, 0, 0, -1.0, 1.0, 0, 3.0, 1.0, Oberflaeche.GLATT),
                quer.zeit(), 1.0, KZ_DAUER_STEHEN));
        BufferedImage q = zeichne(quer, w, h);
        int hell = 0;
        for (int yy = 40; yy < h; yy++) {
            if (helligkeit(q, px, yy) > 200) {
                hell++;
            }
        }
        pruefe(hell > 20, "Kante, die an der Kamera vorbeiläuft, wird an der Bildebene gekürzt und gezeichnet (" + hell + " Pixel)");
    }

    /**
     * Zeichnet ein Spiel in ein Bild (ohne Hilfe, ohne Debug-Karte).
     *
     * @param s Spiel
     * @param w Breite in Pixeln
     * @param h Höhe in Pixeln
     * @return das gezeichnete Bild
     */
    static BufferedImage zeichne(Spiel s, int w, int h) {
        Ansicht a = new Ansicht(s);
        a.setzeHilfe(false);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        a.zeichne(g, w, h);
        g.dispose();
        return img;
    }

    /**
     * Liefert die Helligkeit (größter Farbkanal) eines Bildpunkts.
     *
     * @param img Bild
     * @param x   Spalte
     * @param y   Zeile
     * @return Helligkeit 0 bis 255
     */
    static int helligkeit(BufferedImage img, int x, int y) {
        int rgb = img.getRGB(x, y);
        return Math.max((rgb >> 16) & 0xFF, Math.max((rgb >> 8) & 0xFF, rgb & 0xFF));
    }

    /**
     * Erzeugt ein Level ohne Hindernisse (für den Drifttest).
     *
     * @return leeres Level mit Start in (0 | 0), Blick nach Norden
     */
    static Level leeresLevel() {
        return new Level() {
            @Override
            public String name() {
                return "Leer";
            }

            @Override
            public Welt erzeuge() {
                return new Welt();
            }

            @Override
            public double startX() {
                return 0;
            }

            @Override
            public double startY() {
                return 0;
            }

            @Override
            public double startKurs() {
                return 0;
            }
        };
    }

    /**
     * Erzeugt ein Tastereignis für die Ausrichtungstests.
     *
     * @param id       Objekt-ID
     * @param achse    Achse der Fläche (0 = X, 1 = Y, 2 = Z)
     * @param vz       Vorzeichen der Flächennormalen
     * @param x        X-Koordinate
     * @param y        Y-Koordinate
     * @param z        Z-Koordinate
     * @param t        Spielzeit
     * @param seitlich true bei seitlichem Tasten
     * @return das Tastereignis
     */
    static Ausrichtung.Tastereignis ev(int id, int achse, int vz, double x, double y, double z, double t, boolean seitlich) {
        return new Ausrichtung.Tastereignis(id, achse, vz, x, y, z, t, seitlich);
    }

    /**
     * Prüft die Ausrichtung per Taste: senkrecht bei Tasten nach vorn, parallel bei seitlichem Tasten.
     */
    static void ausrichtungSenkrechtUndParallel() {
        titel("Ausrichtung per Taste: senkrecht bei Tasten nach vorn, parallel bei seitlichem Tasten (Abschnitt 7.3)");
        Spiel s = new Spiel(7);
        int wand = finde(s, "Trennwand West").id;
        s.koerper.x = -4.3;
        s.koerper.y = -0.5;
        s.koerper.kurs = 0.5;
        s.ausrichtung.ereignis(ev(wand, 1, -1, -4.5, 0.5, 1.5, s.zeit(), false));
        s.ausrichtung.ereignis(ev(wand, 1, -1, -4.1, 0.5, 1.5, s.zeit() + 0.5, false));
        pruefe(s.ausrichtung.moeglich(), "Ausrichtung möglich (gleiche Fläche, 40 cm Abstand, 0,5 s Zeitabstand)");
        lauf(s, 1.0);
        pruefe(s.ausrichtung.anzahl() == 0 && Math.abs(s.koerper.kurs - 0.5) < 1e-6, "ohne Taste keine Ausrichtung");
        s.eingabe.ausrichten = true;
        lauf(s, 1.0);
        pruefe(Math.abs(Koerper.normalisiere(s.koerper.kurs)) < 1e-6, "senkrecht: Front zur Wand (Blick Norden)");

        Spiel p = new Spiel(8);
        wand = finde(p, "Trennwand West").id;
        p.koerper.x = -4.3;
        p.koerper.y = -0.5;
        p.koerper.kurs = 0.5;
        p.ausrichtung.ereignis(ev(wand, 1, -1, -4.5, 0.5, 1.5, p.zeit(), true));
        p.ausrichtung.ereignis(ev(wand, 1, -1, -4.1, 0.5, 1.5, p.zeit() + 0.5, true));
        p.eingabe.ausrichten = true;
        lauf(p, 1.0);
        pruefe(Math.abs(Koerper.normalisiere(p.koerper.kurs - Math.PI / 2)) < 1e-6,
                "parallel: kleinerer Drehwinkel gewählt (Blick nach Westen)");
        pruefe(p.ausrichtung.anzahl() == 1, "als Ausrichtung gezählt");
        // nach manueller Drehung innerhalb von 20 s kann erneut ausgerichtet werden
        p.koerper.kurs = 0.5;
        p.eingabe.ausrichten = true;
        lauf(p, 1.0);
        pruefe(p.ausrichtung.anzahl() == 2 && Math.abs(Koerper.normalisiere(p.koerper.kurs - Math.PI / 2)) < 1e-6,
                "Taste richtet nach manueller Drehung erneut aus");
    }

    /**
     * Prüft, dass die Taste ohne gültiges Tastpaar nichts tut und die Möglichkeit nach 20 s verfällt.
     */
    static void ausrichtungNurMitMoeglichkeit() {
        titel("Taste wirkt nur mit gültigem Tastpaar der letzten 20 s (Abschnitt 7.3)");
        Spiel s = new Spiel(9);
        s.koerper.kurs = 0.5;
        s.eingabe.ausrichten = true;
        lauf(s, 1.0);
        pruefe(s.ausrichtung.anzahl() == 0 && Math.abs(s.koerper.kurs - 0.5) < 1e-6,
                "ohne Tastpaar passiert nichts");
        Spiel v = new Spiel(10);
        int wand = finde(v, "Trennwand West").id;
        v.koerper.kurs = 0.5;
        v.ausrichtung.ereignis(ev(wand, 1, -1, -4.5, 0.5, 1.5, 0.0, false));
        v.ausrichtung.ereignis(ev(wand, 1, -1, -4.1, 0.5, 1.5, 0.5, false));
        pruefe(v.ausrichtung.moeglich(), "Möglichkeit besteht");
        v.ausrichtung.update(10.0, v.koerper, v.welt, true, false);
        pruefe(v.ausrichtung.moeglich(), "nach 10 s noch möglich");
        v.ausrichtung.update(25.0, v.koerper, v.welt, true, true);
        pruefe(!v.ausrichtung.moeglich() && v.ausrichtung.anzahl() == 0, "nach 20 s verfallen, Taste wirkt nicht");
    }

    /**
     * Prüft die Kriterien des Tastpaars: Abstand, Fläche, Objekt, Zeitfenster, Zeitabstand, Oberseiten.
     */
    static void ausrichtungKriterien() {
        titel("Tastpaar: Kriterien (Abschnitt 7.2)");
        Ausrichtung a = new Ausrichtung();
        a.ereignis(ev(5, 1, 1, 0, 0, 1, 0, false));
        a.ereignis(ev(5, 1, 1, 0.1, 0, 1, 1, false));
        pruefe(!a.moeglich(), "10 cm Abstand: zu nah");
        a.ereignis(ev(5, 1, 1, 0.95, 0, 1, 2, false));
        pruefe(!a.moeglich(), "95 cm Abstand: zu weit");
        a.ereignis(ev(5, 0, 1, 0.5, 0.0, 1, 3, false));
        pruefe(!a.moeglich(), "andere Fläche (Normale): kein Tastpaar");
        a.ereignis(ev(6, 1, 1, 0.5, 0.0, 1, 4, false));
        pruefe(!a.moeglich(), "anderes Objekt: kein Tastpaar");
        a.ereignis(ev(5, 1, 1, 0.5, 0.0, 1, 30, false));
        pruefe(!a.moeglich(), "mehr als 20 s Abstand: kein Tastpaar");
        a.ereignis(ev(5, 1, 1, 0.9, 0.0, 1, 31, false));
        pruefe(a.moeglich(), "40 cm Abstand innerhalb von 20 s: Ausrichtung möglich");
        Ausrichtung b = new Ausrichtung();
        b.ereignis(ev(5, 2, 1, 0, 0, 1, 0, false));
        b.ereignis(ev(5, 2, 1, 0.5, 0, 1, 1, false));
        pruefe(!b.moeglich(), "Oberseiten (Normale Z) werden nicht ausgerichtet");
        Ausrichtung c = new Ausrichtung();
        c.ereignis(ev(5, 1, 1, 0, 0, 1, 10.0, false));
        c.ereignis(ev(5, 1, 1, 0.5, 0, 1, 10.1, false));
        pruefe(!c.moeglich() && !c.wandfuehrung(), "0,1 s Zeitabstand (gleichzeitig): kein Tastpaar, keine Wandführung");
        c.ereignis(ev(5, 1, 1, 0.3, 0, 1, 10.5, false));
        pruefe(c.moeglich() && c.wandfuehrung(), "0,4 s Zeitabstand zum früheren Ereignis genügt");
    }

    /**
     * Prüft, dass die Wandführung erst ohne Kontakt und bei mehr als 1 m Abstand endet.
     */
    static void wandfuehrungEnde() {
        titel("Wandführung endet erst ohne Kontakt und > 1 m Abstand (Abschnitt 7.4)");
        int wand = 6;
        Spiel nah = new Spiel(11);
        nah.koerper.x = -4.5;
        nah.koerper.y = -0.1;
        nah.ausrichtung.ereignis(ev(wand, 1, -1, -4.7, 0.5, 1.5, nah.zeit(), false));
        nah.ausrichtung.ereignis(ev(wand, 1, -1, -4.3, 0.5, 1.5, nah.zeit() + 0.5, false));
        lauf(nah, 1.0);
        pruefe(nah.ausrichtung.wandfuehrung(), "0,6 m vor der Wand: Wandführung bleibt aktiv (auch ohne Handkontakt)");
        Spiel fern = new Spiel(12);
        fern.koerper.x = 0;
        fern.koerper.y = -1.0;
        fern.ausrichtung.ereignis(ev(wand, 1, -1, -0.2, 0.5, 1.5, fern.zeit(), false));
        fern.ausrichtung.ereignis(ev(wand, 1, -1, 0.2, 0.5, 1.5, fern.zeit() + 0.5, false));
        lauf(fern, 1.0);
        pruefe(!fern.ausrichtung.wandfuehrung(), "mehr als 1 m von allen Flächen entfernt: Wandführung endet");
    }

    /**
     * Prüft die Zufallsdrift: keine Abweichung in den ersten 10 Schritten, danach 1 bis 2 Grad pro Schritt.
     */
    static void drift() {
        titel("Zufallsdrift nach 10 Schritten, nicht davor (Abschnitt 5)");
        Spiel s = new Spiel(13, leeresLevel());
        s.eingabe.vor = true;
        double kursVor11 = Double.NaN;
        for (int i = 0; i < (int) (40 / SIM_DT) && s.koerper.schrittZaehler() < 30; i++) {
            s.schritt(SIM_DT);
            if (s.koerper.schrittZaehler() == 10) {
                kursVor11 = s.koerper.kurs;
            }
        }
        pruefe(Math.abs(kursVor11) < 1e-12, "in den ersten 10 Schritten keine Abweichung");
        lauf(s, SCHRITTLAENGE / GEHGESCHW + 0.01);
        double grad = Math.abs(Math.toDegrees(s.koerper.kurs));
        pruefe(grad >= 20 && grad <= 41, "nach 30 Schritten Abweichung 20-40° (" + String.format("%.1f", grad) + "°)");
        pruefe(s.koerper.kollisionen() == 0, "ohne Hindernis keine Kollision");
    }
}
