package blindspace.tasten;

import blindspace.konfig.Konstanten;
import blindspace.welt.Oberflaeche;

/**
 * Ein ertasteter Punkt: sichtbar als verblassende Kugel (Konzept Abschnitt 8).
 *
 * <p>
 * Im Kurzzeitgedächtnis folgt die Helligkeit {@code H(t) = H0 * M * exp(-k * t)}, wobei k so gewählt ist, dass
 * nach {@link #kurzdauer} (Gehen 15 s, Stehen 30 s) der Restanteil {@link Konstanten#KZ_REST} erreicht ist.
 * Danach übernimmt {@link Tastpunkte} den Punkt ins Langzeitgedächtnis.
 * </p>
 */
public final class Tastpunkt {
    /** X-Koordinate des Tastpunkts in der Welt. */
    public final double x;

    /** Y-Koordinate des Tastpunkts in der Welt. */
    public final double y;

    /** Z-Koordinate des Tastpunkts in der Welt. */
    public final double z;

    /** Spielzeit des Tastvorgangs in Sekunden. */
    public final double t0;

    /** Starthelligkeit H0 (0..1): Stehen 1,0, Gehen 0,5. */
    public final double h0;

    /** Dauer des Kurzzeitgedächtnisses dieses Punkts in Sekunden (Gehen 15 s, Stehen 30 s). */
    public final double kurzdauer;

    /** Durchmesser der Visualisierung in Metern: Stehen 0,20, Gehen 0,10. */
    public final double durchmesser;

    /** ID des ertasteten Objekts. */
    public final int objektId;

    /** Oberflächenart des ertasteten Objekts (bestimmt Muster und Langzeitdauer). */
    public final Oberflaeche oberflaeche;

    /** Kollisionsmultiplikator M (Start 1,0; je Kollision mal dem Faktor des getroffenen Körperteils). */
    public double mult = 1.0;

    /**
     * Erzeugt einen Tastpunkt.
     *
     * @param x           X-Koordinate in der Welt
     * @param y           Y-Koordinate in der Welt
     * @param z           Z-Koordinate in der Welt
     * @param t0          Spielzeit des Tastvorgangs
     * @param h0          Starthelligkeit
     * @param durchmesser Durchmesser der Visualisierung
     * @param kurzdauer   Dauer des Kurzzeitgedächtnisses in Sekunden
     * @param objektId    ID des ertasteten Objekts
     * @param oberflaeche Oberflächenart des ertasteten Objekts
     */
    public Tastpunkt(double x, double y, double z, double t0, double h0, double durchmesser, double kurzdauer,
            int objektId, Oberflaeche oberflaeche) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t0 = t0;
        this.h0 = h0;
        this.durchmesser = durchmesser;
        this.kurzdauer = kurzdauer;
        this.objektId = objektId;
        this.oberflaeche = oberflaeche;
    }

    /**
     * Erzeugt einen Tastpunkt auf einer glatten Oberfläche (Kurzform, z. B. für Tests).
     *
     * @param x           X-Koordinate in der Welt
     * @param y           Y-Koordinate in der Welt
     * @param z           Z-Koordinate in der Welt
     * @param t0          Spielzeit des Tastvorgangs
     * @param h0          Starthelligkeit
     * @param durchmesser Durchmesser der Visualisierung
     * @param kurzdauer   Dauer des Kurzzeitgedächtnisses in Sekunden
     * @param objektId    ID des ertasteten Objekts
     */
    public Tastpunkt(double x, double y, double z, double t0, double h0, double durchmesser, double kurzdauer,
            int objektId) {
        this(x, y, z, t0, h0, durchmesser, kurzdauer, objektId, Oberflaeche.GLATT);
    }

    /**
     * Berechnet die aktuelle Helligkeit {@code H0 * M * exp(-k * (zeit - t0))} mit
     * {@code k = ln(1 / KZ_REST) / kurzdauer}.
     *
     * @param zeit aktuelle Spielzeit in Sekunden
     * @return Helligkeit (0 bis höchstens 1 bei M = 1)
     */
    public double helligkeit(double zeit) {
        double k = Math.log(1.0 / Konstanten.KZ_REST) / kurzdauer;
        return h0 * mult * Math.exp(-k * (zeit - t0));
    }
}
