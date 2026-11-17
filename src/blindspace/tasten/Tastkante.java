package blindspace.tasten;

import blindspace.konfig.Konstanten;
import blindspace.welt.Kantenstueck;

/**
 * Ein ertastetes Kantenstück im räumlichen Gedächtnis (Konzept Abschnitt 8.5).
 *
 * <p>
 * Die Helligkeit folgt zwei Phasen, die ohne Sprung ineinander übergehen: Im Kurzzeitgedächtnis gilt wie bei einem
 * {@link Tastpunkt} {@code H0 * M * exp(-k * t)} bis zum Restanteil {@link Konstanten#KZ_REST}; danach sinkt die
 * Helligkeit linear von diesem Rest auf 0 innerhalb der Langzeitdauer der Oberflächenart. Kanten werden nicht zu
 * Gruppen zusammengefasst und nicht unscharf: Ein Kantenstück bleibt eine scharfe Linie, bis es verschwindet.
 * </p>
 */
public final class Tastkante {
    /** Das ertastete Kantenstück (Geometrie). */
    public final Kantenstueck stueck;

    /** Spielzeit der (letzten) Berührung in Sekunden. */
    public final double t0;

    /** Starthelligkeit H0 (0..1). */
    public final double h0;

    /** Dauer des Kurzzeitgedächtnisses in Sekunden (Gehen 15 s, Stehen 30 s). */
    public final double kurzdauer;

    /** Kollisionsmultiplikator M (Start 1,0; je Kollision mal dem Faktor des getroffenen Körperteils). */
    public double mult = 1.0;

    /**
     * Erzeugt ein Kantenstück im Gedächtnis.
     *
     * @param stueck    das ertastete Kantenstück
     * @param t0        Spielzeit der Berührung
     * @param h0        Starthelligkeit
     * @param kurzdauer Dauer des Kurzzeitgedächtnisses in Sekunden
     */
    public Tastkante(Kantenstueck stueck, double t0, double h0, double kurzdauer) {
        this.stueck = stueck;
        this.t0 = t0;
        this.h0 = h0;
        this.kurzdauer = kurzdauer;
    }

    /**
     * Dauer der linearen Abnahme im Langzeitgedächtnis.
     *
     * @return Dauer in Sekunden ({@code LZ_DAUER} mal Faktor der Oberflächenart)
     */
    private double langDauer() {
        return Konstanten.LZ_DAUER * stueck.oberflaeche().langzeitFaktor();
    }

    /**
     * Berechnet die aktuelle Helligkeit.
     *
     * @param zeit aktuelle Spielzeit in Sekunden
     * @return Helligkeit (0 bis höchstens 1 bei M = 1)
     */
    public double helligkeit(double zeit) {
        double alter = zeit - t0;
        if (alter < kurzdauer) {
            double k = Math.log(1.0 / Konstanten.KZ_REST) / kurzdauer;
            return h0 * mult * Math.exp(-k * alter);
        }
        double rest = h0 * mult * Konstanten.KZ_REST;
        return rest * Math.max(0, 1 - (alter - kurzdauer) / langDauer());
    }

    /**
     * Ist das Kantenstück vollständig verblasst?
     *
     * @param zeit aktuelle Spielzeit in Sekunden
     * @return true, wenn es gelöscht werden kann
     */
    public boolean abgelaufen(double zeit) {
        return zeit - t0 >= kurzdauer + langDauer();
    }
}
