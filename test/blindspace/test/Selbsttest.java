package blindspace.test;

import static blindspace.konfig.Konstanten.*;

import blindspace.levels.Level;
import blindspace.spiel.Spiel;
import blindspace.spieler.Ausrichtung;
import blindspace.spieler.Haende;
import blindspace.spieler.Koerper;
import blindspace.tasten.Tastpunkt;
import blindspace.tasten.Tastpunkte;
import blindspace.welt.Beruehrung;
import blindspace.welt.Koerperteil;
import blindspace.welt.Objektart;
import blindspace.welt.Oberflaeche;
import blindspace.welt.Quader;
import blindspace.welt.Welt;

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
        gehenNurAufrecht();
        koerperteile();
        oberflaechen();
        einzelUndVollschritt();
        zufallsStart();
        handLoesenUndGleiten();
        seitlichesTastenStopptNichtUndRichtetAus();
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
        // Gehen ist geduckt nicht erlaubt
        double y0 = s.koerper.y;
        s.eingabe.vor = true;
        lauf(s, 1.0);
        pruefe(Math.abs(s.koerper.y - y0) < 1e-9, "geduckt ist Gehen gesperrt");
    }

    /**
     * Prüft, dass Gehen nur aufrecht möglich ist, Drehen aber in jeder Körperstufe.
     */
    static void gehenNurAufrecht() {
        titel("Gehen nur aufrecht (Abschnitt 4.3)");
        Spiel s = new Spiel(4);
        s.eingabe.stufeWunsch = 1;
        s.eingabe.vor = true;
        lauf(s, 1.5);
        pruefe(Math.abs(s.koerper.y - s.level().startY()) < 1e-9, "Stufe 70 cm: keine Bewegung");
        s.eingabe.links = true;
        lauf(s, 0.1);
        s.eingabe.links = false;
        lauf(s, 0.5);
        double d = Math.abs(Koerper.normalisiere(s.koerper.kurs));
        pruefe(Math.abs(d - Math.toRadians(15)) <= Math.toRadians(1) + 1e-9,
                "Drehen geduckt möglich: " + String.format("%.1f", Math.toDegrees(d)) + "°");
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
     * Prüft, dass seitlicher Handkontakt den Körper nicht stoppt, die Wandführung startet und die Leertaste
     * danach parallel ausrichtet.
     */
    static void seitlichesTastenStopptNichtUndRichtetAus() {
        titel("Seitliches Tasten im Gehen: Körper läuft weiter, Ausrichtung nur auf Tastendruck (Abschnitte 4.5, 7)");
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
        pruefe(s.ausrichtung.wandfuehrung(), "Wandführung bereits ohne Ausrichtung aktiv");
        pruefe(s.ausrichtung.anzahl() == 0, "keine automatische Ausrichtung");
        pruefe(Math.abs(Koerper.normalisiere(s.koerper.kurs + 0.10)) < 1e-6, "Kurs unverändert");
        s.eingabe.ausrichten = true;
        lauf(s, 1.0);
        pruefe(s.ausrichtung.anzahl() == 1, "genau eine Ausrichtung nach Tastendruck");
        pruefe(Math.abs(Koerper.normalisiere(s.koerper.kurs)) < Math.toRadians(1.0),
                "parallel zur Wand (Blick nach Norden): " + String.format("%.2f", Math.toDegrees(s.koerper.kurs)) + "°");
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
