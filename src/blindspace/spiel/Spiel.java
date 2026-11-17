package blindspace.spiel;

import static blindspace.konfig.Konstanten.*;

import blindspace.levels.Level;
import blindspace.levels.Testlevel;
import blindspace.spieler.Ausrichtung;
import blindspace.spieler.Eingabe;
import blindspace.spieler.Haende;
import blindspace.spieler.Koerper;
import blindspace.tasten.Tastpunkt;
import blindspace.tasten.Tastpunkte;
import blindspace.welt.Beruehrung;
import blindspace.welt.Oberflaeche;
import blindspace.welt.Welt;
import java.util.Random;

/**
 * Spielkern ohne Oberfläche: verbindet Level, Welt, Körper, Hände, Tastpunkte und manuelle Ausrichtung.
 *
 * <p>
 * Die Simulation läuft in festen Zeitschritten ({@code SIM_DT}). {@link #schritt(double)} legt die
 * Reihenfolge eines Schritts fest: Ausrichtungswunsch abholen, Körper bewegen, Kollision melden, Hände
 * bewegen, Handstopp prüfen, Tastpunkte und Tastereignisse erzeugen, Ausrichtung aktualisieren,
 * Tastpunkte altern lassen.
 * </p>
 */
public final class Spiel {
    /** Eingabezustand (von der Anzeige befüllt). */
    public final Eingabe eingabe = new Eingabe();

    /** Das geladene Level (Welt und Startpose). */
    private final Level level;

    /** Die Welt des geladenen Levels. */
    public final Welt welt;

    /** Die beiden Hände. */
    public final Haende haende = new Haende();

    /** Alle aktiven Tastpunkte. */
    public final Tastpunkte tastpunkte = new Tastpunkte();

    /** Manuelle Ausrichtung und Wandführung. */
    public final Ausrichtung ausrichtung = new Ausrichtung();

    /** Startwert des Zufallsgenerators (gleich bleibend bei Neustart). */
    private final long seed;

    /** Startrichtung bei jedem Start zufällig wählen. */
    private final boolean zufallsKurs;

    /** Zufallsquelle für die Startrichtung (läuft über Neustarts weiter). */
    private final Random startZufall = new Random();

    /** Der Spielerkörper; wird bei {@link #reset()} neu erzeugt. */
    public Koerper koerper;

    /** Spielzeit in Sekunden. */
    private double zeit;

    /** Akkumulator für nicht verbrauchte Frame-Zeit. */
    private double akku;

    /**
     * Startet das Testlevel mit zufälligem Seed und zufälliger Startrichtung (auch bei jedem Neustart).
     */
    public Spiel() {
        this(new Random().nextLong(), new Testlevel(), true);
    }

    /**
     * Startet das Testlevel mit festem Seed (reproduzierbar, z. B. für Tests).
     *
     * @param seed Startwert des Zufallsgenerators (Drift, Drehabweichung)
     */
    public Spiel(long seed) {
        this(seed, new Testlevel());
    }

    /**
     * Startet ein beliebiges Level mit der festen Startrichtung des Levels (reproduzierbar).
     *
     * @param seed  Startwert des Zufallsgenerators
     * @param level zu ladendes Level
     */
    public Spiel(long seed, Level level) {
        this(seed, level, false);
    }

    /**
     * Startet ein beliebiges Level.
     *
     * @param seed          Startwert des Zufallsgenerators
     * @param level         zu ladendes Level
     * @param zufallsKurs   true: Startrichtung bei jedem Start zufällig; false: {@code level.startKurs()}
     */
    private Spiel(long seed, Level level, boolean zufallsKurs) {
        this.zufallsKurs = zufallsKurs;
        this.seed = seed;
        this.level = level;
        this.welt = level.erzeuge();
        reset();
    }

    /** Setzt das Spiel auf den Startzustand des Levels zurück (Körper, Hände, Tastpunkte, Ausrichtung, Eingabe). */
    public void reset() {
        double kurs = zufallsKurs ? (startZufall.nextDouble() * 2 - 1) * Math.PI : level.startKurs();
        koerper = new Koerper(new Random(seed), level.startX(), level.startY(), kurs);
        haende.reset();
        tastpunkte.leeren();
        ausrichtung.reset();
        eingabe.zuruecksetzen();
        zeit = 0;
        akku = 0;
    }

    /**
     * Das geladene Level.
     *
     * @return Level mit Name und Startpose
     */
    public Level level() {
        return level;
    }

    /**
     * Spielzeit seit dem Start oder Neustart.
     *
     * @return Zeit in Sekunden
     */
    public double zeit() {
        return zeit;
    }

    /**
     * Rechnet die seit dem letzten Aufruf vergangene Zeit in festen Simulationsschritten ab.
     *
     * @param dtReal vergangene echte Zeit in Sekunden (wird auf {@code MAX_FRAME_DT} begrenzt)
     */
    public void aktualisiere(double dtReal) {
        akku += Math.min(dtReal, MAX_FRAME_DT);
        while (akku >= SIM_DT) {
            schritt(SIM_DT);
            akku -= SIM_DT;
        }
    }

    /**
     * Berührt eine nach vorn tastende Hand eine Fläche, die der Blickrichtung entgegenzeigt? Seitlich
     * vorbeistreifende Wände und Oberseiten blockieren das Gehen nicht.
     *
     * @return true, wenn Vorwärtsgehen durch Handkontakt blockiert ist
     */
    private boolean vorneBlockiert() {
        double fx = -Math.sin(koerper.kurs), fy = Math.cos(koerper.kurs);
        for (Haende.Hand h : haende.alle()) {
            if (h.seitlich || h.kontakt == null) {
                continue;
            }
            if (h.kontakt.nx() * fx + h.kontakt.ny() * fy < -0.3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt einen Tastpunkt mit den Werten des Modus: Gehen 10 cm, Helligkeit 50 %, 15 s Kurzzeitgedächtnis;
     * Stehen 20 cm, 100 %, 30 s.
     *
     * @param px       X-Koordinate des Punkts
     * @param py       Y-Koordinate des Punkts
     * @param pz       Z-Koordinate des Punkts
     * @param gehen    true für die Werte im Gehen (auch bei Kollisionen), false für die im Stehen
     * @param objektId ID des berührten Objekts
     * @param oberflaeche Oberflächenart des berührten Objekts
     * @return der neue Tastpunkt zur aktuellen Spielzeit
     */
    private Tastpunkt neuerPunkt(double px, double py, double pz, boolean gehen, int objektId,
            Oberflaeche oberflaeche) {
        return new Tastpunkt(px, py, pz, zeit, gehen ? HELLIGKEIT_GEHEN : HELLIGKEIT_STEHEN,
                gehen ? PUNKT_DURCHMESSER_GEHEN : PUNKT_DURCHMESSER_STEHEN,
                gehen ? KZ_DAUER_GEHEN : KZ_DAUER_STEHEN, objektId, oberflaeche);
    }

    /**
     * Ein Simulationsschritt in fester Reihenfolge (siehe Klassenbeschreibung).
     *
     * @param dt Zeitschritt in Sekunden (üblicherweise {@code SIM_DT})
     */
    public void schritt(double dt) {
        zeit += dt;
        boolean ausrichtenWunsch = eingabe.ausrichten;
        eingabe.ausrichten = false;
        koerper.update(dt, eingabe, welt, ausrichtung.wandfuehrung(), vorneBlockiert());
        Welt.Kollision stoss = koerper.holeKollision();
        if (stoss != null) {
            // Kollisionsereignis: erst alle vorhandenen Visualisierungen dämpfen, dann den Kollisionspunkt
            // mit den Regeln eines Tastpunkts im Gehen anlegen
            tastpunkte.kollision(stoss.teil().faktor(), zeit);
            tastpunkte.hinzufuegen(
                    neuerPunkt(stoss.px(), stoss.py(), stoss.pz(), true, stoss.objektId(), stoss.oberflaeche()));
        }
        haende.update(dt, koerper, welt, eingabe);

        // Handkontakt der vorderen Hand beim Vorwärtsgehen: Körper bleibt stehen
        if (koerper.schrittAktiv() && koerper.schrittRichtung() > 0 && vorneBlockiert()) {
            koerper.stoppeSchritt();
        }

        // Tastpunkte (höchstens einer pro Hand und 0,5 s) und Tastereignisse
        boolean gehen = koerper.gehen();
        boolean kontakt = false;
        for (Haende.Hand h : haende.alle()) {
            h.tastTimer -= dt;
            if (h.kontakt == null) {
                continue;
            }
            kontakt = true;
            if (h.tastTimer <= 0) {
                Beruehrung b = h.kontakt;
                tastpunkte.hinzufuegen(neuerPunkt(b.px(), b.py(), b.pz(), gehen, b.objektId(), b.oberflaeche()));
                h.tastTimer = TASTINTERVALL;
                ausrichtung.ereignis(new Ausrichtung.Tastereignis(b.objektId(), b.achse(), b.vorzeichen(), b.px(),
                        b.py(), b.pz(), zeit, h.seitlich));
            }
        }
        ausrichtung.update(zeit, koerper, welt, kontakt, ausrichtenWunsch);
        tastpunkte.aufraeumen(zeit);
    }
}
