package blindspace.welt;

import blindspace.konfig.Konstanten;

/**
 * Körperteil, das bei einer Körperkollision zuerst getroffen wurde. Jedes Teil dämpft die Helligkeit der
 * Erinnerung unterschiedlich stark (Konzept Abschnitt 6).
 */
public enum Koerperteil {
    /** Beinzylinder (der Fuß berührt Hindernisse in der Regel zuerst). */
    BEIN(Konstanten.KOLLISION_FAKTOR_BEIN),

    /** Oberkörper-Quader. */
    RUMPF(Konstanten.KOLLISION_FAKTOR_RUMPF),

    /** Kopfzylinder. */
    KOPF(Konstanten.KOLLISION_FAKTOR_KOPF);

    /** Faktor, mit dem eine Kollision die Helligkeit aller aktiven Visualisierungen multipliziert. */
    private final double faktor;

    /**
     * Erzeugt ein Körperteil mit seinem Dämpfungsfaktor.
     *
     * @param faktor Helligkeitsfaktor (0 bis 1)
     */
    Koerperteil(double faktor) {
        this.faktor = faktor;
    }

    /**
     * Helligkeitsfaktor dieses Körperteils.
     *
     * @return Faktor zwischen 0 und 1
     */
    public double faktor() {
        return faktor;
    }
}
