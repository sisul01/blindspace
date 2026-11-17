package blindspace.spieler;

import static blindspace.konfig.Konstanten.*;

import blindspace.welt.Welt;
import java.util.Random;

/**
 * Der unsichtbare Spielerkörper (Konzept Abschnitte 3, 5 und 6).
 *
 * <p>
 * Schritte: Bei gehaltener Taste folgen Vollschritte (0,5 m) bis zum Loslassen; ein kurzer Tastendruck
 * (Taste vor Erreichen des halben Schritts losgelassen) ergibt einen Halbschritt (0,25 m).
 * Die Klasse verwaltet Position, Blickrichtung, Körperstufe (Ducken), Schritte, Drehungen, die Zufallsdrift,
 * den Gehen/Stehen-Modus sowie Körperkollisionen mit Rückstoß und Eingabesperre. Sie kennt nur die Welt für
 * die Kollisionsprüfung; Hände und Tastpunkte werden von {@code blindspace.spiel.Spiel} koordiniert.
 * </p>
 *
 * <p>
 * Winkel: 0 = nach +Y (Norden), positiv gegen den Uhrzeigersinn. Die Vorwärtsrichtung ist daher
 * {@code (-sin(kurs), cos(kurs))}.
 * </p>
 */
public final class Koerper {
    /** Zufallsquelle für Drift und Drehabweichung. */
    private final Random zufall;

    /** Seite der Drift (+1 oder -1), pro Sitzung zufällig festgelegt. */
    private final int driftSeite;

    /** X-Position der Körpermitte in der Welt. */
    public double x;

    /** Y-Position der Körpermitte in der Welt. */
    public double y;

    /** Blickrichtung in Radiant (0 = +Y, positiv gegen den Uhrzeigersinn). */
    public double kurs;

    /** Körperstufe 0 (aufrecht) bis 3 (geduckt). */
    private int stufe;

    /** Augenhöhe, folgt der Körperstufe weich (nur für die Darstellung). */
    private double augenZ;

    /** Ein Schritt läuft gerade. */
    private boolean schrittAktiv;

    /** Noch zu gehende Strecke des laufenden Schritts. */
    private double schrittRest;

    /** Richtung des laufenden Schritts: +1 vorwärts, -1 rückwärts. */
    private int schrittRichtung;

    /** Der laufende Schritt wurde bereits zum Halbschritt verkürzt (Taste früh losgelassen). */
    private boolean schrittGekuerzt;

    /** Ein weiterer Schritt folgt bei gehaltener Taste auf einen vollendeten Schritt (kein Einzeldruck). */
    private boolean schrittFolge;

    /** Gesamte Driftdrehung des laufenden Schritts in Radiant. */
    private double schrittDrift;

    /** Anzahl Schritte seit dem letzten Zurücksetzen (Drift beginnt nach {@code DRIFT_START_SCHRITTE}). */
    private int schrittZaehler;

    /** Eine Drehanimation läuft gerade. */
    private boolean drehAktiv;

    /** Verstrichene Zeit der Drehanimation. */
    private double drehT;

    /** Gesamtwinkel der laufenden Drehung. */
    private double drehGesamt;

    /** Bisher ausgeführter Anteil der laufenden Drehung in Radiant. */
    private double drehBisher;

    /** Noch zurückzulegender Rückstoß nach einer Kollision. */
    private double rueckstossRest;

    /** X-Anteil der Rückstoßrichtung (Welt). */
    private double rueckstossDx;

    /** Y-Anteil der Rückstoßrichtung (Welt). */
    private double rueckstossDy;

    /** Restzeit der Eingabesperre nach einer Kollision. */
    private double sperre;

    /** Anzahl bisheriger Körperkollisionen. */
    private int kollisionen;

    /** Letzte Kollision, die noch nicht über {@link #holeKollision()} abgeholt wurde, sonst null. */
    private Welt.Kollision offeneKollision;

    /** Gehen-Modus (mit Nachlauf), nur aufrecht möglich. */
    private boolean gehen;

    /** Restzeit des Gehen-Nachlaufs. */
    private double nachlauf;

    /** Gehsperre nach einem Handstopp: Die Gehtaste muss losgelassen und neu gedrückt werden. */
    private boolean gehSperre;

    /**
     * Erzeugt einen Körper aufrecht an der Startpose.
     *
     * @param zufall    Zufallsquelle (bestimmt auch die Driftseite)
     * @param startX    Start-X-Position
     * @param startY    Start-Y-Position
     * @param startKurs Startausrichtung in Radiant
     */
    public Koerper(Random zufall, double startX, double startY, double startKurs) {
        this.zufall = zufall;
        this.driftSeite = zufall.nextBoolean() ? 1 : -1;
        this.x = startX;
        this.y = startY;
        this.kurs = startKurs;
        this.augenZ = BEIN_HOEHEN[0] + AUGEN_OFFSET;
    }

    // ===== Zustand =====

    /**
     * Aktuelle Körperstufe.
     *
     * @return 0 (aufrecht) bis 3 (geduckt)
     */
    public int stufe() {
        return stufe;
    }

    /**
     * Höhe des Beinzylinders der aktuellen Stufe.
     *
     * @return Höhe in Metern
     */
    public double beinHoehe() {
        return BEIN_HOEHEN[stufe];
    }

    /**
     * Augenhöhe für die Kamera (folgt der Stufe weich).
     *
     * @return Z-Koordinate der Augen
     */
    public double augenZ() {
        return augenZ;
    }

    /**
     * Gehen-Modus: ein Schritt läuft oder eine Gehtaste ist gedrückt, plus 0,3 s Nachlauf; nur aufrecht.
     *
     * @return true im Gehen-Modus, false im Stehen-Modus
     */
    public boolean gehen() {
        return gehen;
    }

    /**
     * Läuft gerade ein Schritt?
     *
     * @return true bei laufendem Schritt
     */
    public boolean schrittAktiv() {
        return schrittAktiv;
    }

    /**
     * Richtung des laufenden Schritts.
     *
     * @return +1 vorwärts, -1 rückwärts (nur bei laufendem Schritt aussagekräftig)
     */
    public int schrittRichtung() {
        return schrittRichtung;
    }

    /**
     * Läuft gerade eine Drehanimation?
     *
     * @return true bei laufender Drehung
     */
    public boolean drehAktiv() {
        return drehAktiv;
    }

    /**
     * Anzahl der bisherigen Körperkollisionen.
     *
     * @return Kollisionszähler
     */
    public int kollisionen() {
        return kollisionen;
    }

    /**
     * Schrittzähler für die Drift (bleibt 0, solange die Wandführung aktiv ist).
     *
     * @return Anzahl Schritte seit dem letzten Zurücksetzen
     */
    public int schrittZaehler() {
        return schrittZaehler;
    }

    /**
     * Restzeit der Eingabesperre nach einer Kollision.
     *
     * @return Restzeit in Sekunden (0 = keine Sperre)
     */
    public double sperre() {
        return sperre;
    }

    /**
     * Meldet jede Kollision genau einmal (danach {@code null} bis zur nächsten Kollision).
     *
     * @return die Kollision mit Körperteil und Kontaktpunkt oder {@code null}, wenn seit dem letzten Aufruf
     *         keine stattfand
     */
    public Welt.Kollision holeKollision() {
        Welt.Kollision k = offeneKollision;
        offeneKollision = null;
        return k;
    }

    // ===== Aktionen =====

    /**
     * Bricht den laufenden Schritt ab, weil eine nach vorn tastende Hand eine Fläche berührt (Konzept 4.5).
     * Danach muss die Gehtaste losgelassen und neu gedrückt werden.
     */
    public void stoppeSchritt() {
        schrittAktiv = false;
        schrittRest = 0;
        gehSperre = true;
    }

    /**
     * Startet eine animierte Drehung um genau {@code delta} Radiant. Wird für Drehschritte und die
     * automatische Ausrichtung verwendet.
     *
     * @param delta Drehwinkel in Radiant (positiv = nach links, gegen den Uhrzeigersinn)
     */
    public void drehe(double delta) {
        drehAktiv = true;
        drehT = 0;
        drehGesamt = delta;
        drehBisher = 0;
    }

    /**
     * Normalisiert einen Winkel auf das Intervall (-π, π].
     *
     * @param a Winkel in Radiant
     * @return äquivalenter Winkel in (-π, π]
     */
    public static double normalisiere(double a) {
        while (a > Math.PI) {
            a -= 2 * Math.PI;
        }
        while (a <= -Math.PI) {
            a += 2 * Math.PI;
        }
        return a;
    }

    // ===== Simulation =====

    /**
     * Ein Simulationsschritt des Körpers: Eingaben auswerten, Schritte und Drehungen starten und ausführen,
     * Kollisionen behandeln, Gehen/Stehen-Modus bestimmen.
     *
     * @param dt             Zeitschritt in Sekunden
     * @param in             aktueller Eingabezustand (die Stufenwahl wird verbraucht)
     * @param welt           Welt für die Kollisionsprüfung
     * @param wandfuehrung   true, solange die Wandführung aktiv ist (Drift aus, Schrittzähler bleibt 0)
     * @param vorneBlockiert eine nach vorn tastende Hand berührt eine Fläche, die der Gehrichtung entgegenzeigt
     *                       (Konzept 4.5): Vorwärtsgehen ist dann nicht möglich, die Gehtaste muss neu gedrückt
     *                       werden
     */
    public void update(double dt, Eingabe in, Welt welt, boolean wandfuehrung, boolean vorneBlockiert) {
        if (sperre > 0) {
            sperre = Math.max(0, sperre - dt);
        }
        int wunsch = in.stufeWunsch;
        if (wunsch >= 0) {
            stufe = Math.min(wunsch, BEIN_HOEHEN.length - 1);
            in.stufeWunsch = -1;
        }
        if (!in.vor && !in.rueck) {
            gehSperre = false;
            if (!schrittAktiv) {
                schrittFolge = false;
            }
        }
        if (wandfuehrung) {
            schrittZaehler = 0;
        }

        boolean gesperrt = sperre > 0;
        boolean aufrecht = stufe == 0;
        int richtung = (in.vor ? 1 : 0) - (in.rueck ? 1 : 0);
        if (vorneBlockiert && richtung > 0) {
            if (schrittAktiv && schrittRichtung > 0) {
                stoppeSchritt();
            }
            gehSperre = true;
        }

        // Schritt starten
        if (!gesperrt && aufrecht && !schrittAktiv && rueckstossRest <= 0 && !gehSperre && richtung != 0) {
            schrittAktiv = true;
            schrittRest = SCHRITTLAENGE;
            schrittRichtung = richtung;
            schrittGekuerzt = schrittFolge; // nur der erste Schritt eines Tastendrucks kann ein Halbschritt sein
            schrittDrift = 0;
            if (!wandfuehrung) {
                if (schrittZaehler >= DRIFT_START_SCHRITTE) {
                    schrittDrift = driftSeite * (DRIFT_MIN + zufall.nextDouble() * (DRIFT_MAX - DRIFT_MIN));
                }
                schrittZaehler++;
            }
        }
        // Drehung starten
        if (!gesperrt && !drehAktiv) {
            int d = (in.links ? 1 : 0) - (in.rechts ? 1 : 0);
            if (d != 0) {
                double zufallsAnteil = (zufall.nextDouble() * 2 - 1) * DREH_ZUFALL;
                drehe(d * DREH_SCHRITT + zufallsAnteil);
            }
        }

        // Drehung animieren
        if (drehAktiv) {
            drehT = Math.min(DREH_DAUER, drehT + dt);
            double s = drehT / DREH_DAUER;
            double e = s * s * (3 - 2 * s);
            double neu = drehGesamt * e;
            kurs = normalisiere(kurs + (neu - drehBisher));
            drehBisher = neu;
            if (drehT >= DREH_DAUER) {
                drehAktiv = false;
            }
        }

        // Kurzer Tastendruck: Taste vor Erreichen des halben Schritts losgelassen -> Halbschritt
        if (schrittAktiv && !schrittGekuerzt) {
            boolean taste = schrittRichtung > 0 ? in.vor : in.rueck;
            if (!taste) {
                schrittGekuerzt = true;
                if (schrittRest > HALBSCHRITT) {
                    schrittRest -= HALBSCHRITT;
                }
            }
        }

        // Schritt ausführen
        if (schrittAktiv) {
            double d = Math.min(GEHGESCHW * dt, schrittRest);
            kurs = normalisiere(kurs + schrittDrift * (d / SCHRITTLAENGE));
            double nx = x + (-Math.sin(kurs)) * schrittRichtung * d;
            double ny = y + Math.cos(kurs) * schrittRichtung * d;
            Welt.Kollision treffer = welt.koerperKollision(nx, ny, kurs, stufe);
            if (treffer != null) {
                kollision(schrittRichtung, treffer);
            } else {
                x = nx;
                y = ny;
                schrittRest -= d;
                if (schrittRest <= 1e-9) {
                    schrittAktiv = false;
                    schrittFolge = true;
                }
            }
        }

        // Rückstoß ausführen
        if (rueckstossRest > 0) {
            double d = Math.min(GEHGESCHW * dt, rueckstossRest);
            double nx = x + rueckstossDx * d;
            double ny = y + rueckstossDy * d;
            if (welt.koerperKollidiert(nx, ny, kurs, stufe)) {
                rueckstossRest = 0;
            } else {
                x = nx;
                y = ny;
                rueckstossRest -= d;
            }
        }

        // Gehen/Stehen (mit Nachlauf gegen Flackern)
        boolean tasteGehen = aufrecht && !gesperrt && !gehSperre && richtung != 0;
        if (schrittAktiv || tasteGehen) {
            nachlauf = GEHEN_NACHLAUF;
        } else {
            nachlauf = Math.max(0, nachlauf - dt);
        }
        gehen = aufrecht && (schrittAktiv || tasteGehen || nachlauf > 0);

        // Augenhöhe weich nachführen (nur Darstellung)
        double zielZ = BEIN_HOEHEN[stufe] + AUGEN_OFFSET;
        double dz = zielZ - augenZ;
        double maxDz = 2.0 * dt;
        augenZ += Math.max(-maxDz, Math.min(maxDz, dz));
    }

    /**
     * Behandelt eine Körperkollision: Schritt abbrechen, Eingabesperre starten, Kollision zählen und melden
     * sowie den Rückstoß (halber Schritt entgegen der Bewegungsrichtung) einleiten.
     *
     * @param richtung Bewegungsrichtung des Schritts, bei dem die Kollision auftrat (+1 vorwärts, -1 rückwärts)
     * @param treffer  die Kollision (Körperteil und Kontaktpunkt) für die Meldung an das Spiel
     */
    private void kollision(int richtung, Welt.Kollision treffer) {
        schrittAktiv = false;
        schrittRest = 0;
        sperre = EINGABESPERRE;
        kollisionen++;
        offeneKollision = treffer;
        double fx = -Math.sin(kurs), fy = Math.cos(kurs);
        rueckstossDx = -richtung * fx;
        rueckstossDy = -richtung * fy;
        rueckstossRest = RUECKSTOSS;
    }
}
