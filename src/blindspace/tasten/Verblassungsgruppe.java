package blindspace.tasten;

import blindspace.konfig.Konstanten;
import blindspace.welt.Oberflaeche;

/**
 * Eine Verblassungsgruppe des Langzeitgedächtnisses: mehrere Tastpunkte einer Raumzelle, die nach dem
 * Kurzzeitgedächtnis zu einer einzigen, zunehmend unscharfen Visualisierung zusammengefasst wurden.
 *
 * <p>
 * Die Helligkeit sinkt linear von der Helligkeit zum Zeitpunkt der letzten Aufnahme ({@code tRef}) bis auf 0 zum
 * Zeitpunkt {@code tEnde}. Der Radius wächst dabei (Unschärfe). Nimmt die Gruppe einen weiteren Punkt auf, gelten
 * die größere Helligkeit und das spätere Ende.
 * </p>
 */
public final class Verblassungsgruppe {
    /** X-Koordinate des Gruppenmittelpunkts (Mittel der Mitglieder). */
    private double x;

    /** Y-Koordinate des Gruppenmittelpunkts. */
    private double y;

    /** Z-Koordinate des Gruppenmittelpunkts. */
    private double z;

    /** Oberflächenart der Mitglieder (Muster, Langzeitdauer); alle Mitglieder haben dieselbe. */
    private final Oberflaeche oberflaeche;

    /** Dauer dieser Gruppe im Langzeitgedächtnis in Sekunden (abhängig von der Oberflächenart). */
    private final double dauer;

    /** Anzahl der aufgenommenen Punkte. */
    private int anzahl;

    /** Grundradius der Gruppe (ohne Unschärfe) in Metern. */
    private double radius;

    /** Zeitpunkt der letzten Aufnahme oder Dämpfung; Bezugspunkt der linearen Abnahme. */
    private double tRef;

    /** Helligkeit zum Zeitpunkt {@code tRef}. */
    private double hRef;

    /** Zeitpunkt, an dem die Gruppe vollständig verschwindet. */
    private double tEnde;

    /**
     * Erzeugt eine Gruppe aus einem ersten Punkt.
     *
     * @param px     X-Koordinate des Punkts
     * @param py     Y-Koordinate des Punkts
     * @param pz     Z-Koordinate des Punkts
     * @param r      Radius des Punkts
     * @param zeit   Zeitpunkt der Übernahme ins Langzeitgedächtnis
     * @param hell   Helligkeit zu diesem Zeitpunkt
     * @param obfl   Oberflächenart der Mitglieder
     */
    Verblassungsgruppe(double px, double py, double pz, double r, double zeit, double hell, Oberflaeche obfl) {
        oberflaeche = obfl;
        dauer = Konstanten.LZ_DAUER * obfl.langzeitFaktor();
        x = px;
        y = py;
        z = pz;
        anzahl = 1;
        radius = r;
        tRef = zeit;
        hRef = hell;
        tEnde = zeit + dauer;
    }

    /**
     * Nimmt einen weiteren Punkt auf: Mittelpunkt und Radius werden angepasst, es gelten die größere der beiden
     * Helligkeiten und das spätere Ende.
     *
     * @param px   X-Koordinate des Punkts
     * @param py   Y-Koordinate des Punkts
     * @param pz   Z-Koordinate des Punkts
     * @param r    Radius des Punkts
     * @param zeit Zeitpunkt der Übernahme
     * @param hell Helligkeit des Punkts zu diesem Zeitpunkt
     */
    void nimmAuf(double px, double py, double pz, double r, double zeit, double hell) {
        double jetzt = helligkeit(zeit);
        x = (x * anzahl + px) / (anzahl + 1);
        y = (y * anzahl + py) / (anzahl + 1);
        z = (z * anzahl + pz) / (anzahl + 1);
        anzahl++;
        double abstand = Math.sqrt((px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z));
        radius = Math.max(radius, Math.max(r, abstand + r));
        hRef = Math.max(jetzt, hell);
        tRef = zeit;
        tEnde = Math.max(tEnde, zeit + dauer);
    }

    /**
     * Dämpft die Gruppe nach einer Körperkollision.
     *
     * @param zeit   aktuelle Spielzeit
     * @param faktor Helligkeitsfaktor des getroffenen Körperteils
     */
    void daempfe(double zeit, double faktor) {
        hRef = helligkeit(zeit) * faktor;
        tRef = zeit;
    }

    /**
     * Aktuelle Helligkeit: linear von {@code hRef} bei {@code tRef} auf 0 bei {@code tEnde}.
     *
     * @param zeit aktuelle Spielzeit
     * @return Helligkeit (0 bis 1)
     */
    public double helligkeit(double zeit) {
        if (zeit >= tEnde) {
            return 0;
        }
        return hRef * Math.max(0, (tEnde - zeit) / (tEnde - tRef));
    }

    /**
     * Darstellungsradius: der Grundradius wächst mit dem Verblassen um bis zu {@code LZ_UNSCHAERFE}.
     *
     * @param zeit aktuelle Spielzeit
     * @return Radius in Metern
     */
    public double radius(double zeit) {
        double rest = Math.max(0, Math.min(1, (tEnde - zeit) / dauer));
        return radius * (1 + Konstanten.LZ_UNSCHAERFE * (1 - rest));
    }

    /**
     * Ist die Gruppe vollständig verblasst?
     *
     * @param zeit aktuelle Spielzeit
     * @return true, wenn die Gruppe gelöscht werden kann
     */
    boolean abgelaufen(double zeit) {
        return zeit >= tEnde;
    }

    /**
     * Oberflächenart der Mitglieder.
     *
     * @return Oberflächenart (bestimmt das Darstellungsmuster)
     */
    public Oberflaeche oberflaeche() {
        return oberflaeche;
    }

    /**
     * X-Koordinate des Mittelpunkts.
     *
     * @return X in Metern
     */
    public double x() {
        return x;
    }

    /**
     * Y-Koordinate des Mittelpunkts.
     *
     * @return Y in Metern
     */
    public double y() {
        return y;
    }

    /**
     * Z-Koordinate des Mittelpunkts.
     *
     * @return Z in Metern
     */
    public double z() {
        return z;
    }

    /**
     * Anzahl der zusammengefassten Tastpunkte.
     *
     * @return Anzahl der Mitglieder
     */
    public int anzahl() {
        return anzahl;
    }
}
