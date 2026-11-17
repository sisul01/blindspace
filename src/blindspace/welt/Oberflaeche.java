package blindspace.welt;

import blindspace.konfig.Konstanten;

/**
 * Oberflächenart eines Objekts (GDD Abschnitt 3). Sie bestimmt das Muster der Tastpunkt-Visualisierung (die
 * Darstellung ist ausschließlich schwarz-weiß) und die Dauer im Langzeitgedächtnis.
 */
public enum Oberflaeche {
    /** Rau, z. B. Beton, rauer Putz oder Ziegel: grobes Punktmuster. */
    RAU(1.0),

    /** Glatt, z. B. Metall oder Kunststoff: gleichmäßige, glatte Darstellung. */
    GLATT(1.0),

    /** Strukturiert, z. B. Holz oder gerillte Flächen: Streifen- bzw. Maserungsmuster, verblasst schneller. */
    STRUKTURIERT(Konstanten.LZ_FAKTOR_STRUKTURIERT);

    /** Faktor auf die Dauer im Langzeitgedächtnis (1,0 = volle Dauer). */
    private final double langzeitFaktor;

    /**
     * Erzeugt eine Oberflächenart.
     *
     * @param langzeitFaktor Faktor auf {@code LZ_DAUER}
     */
    Oberflaeche(double langzeitFaktor) {
        this.langzeitFaktor = langzeitFaktor;
    }

    /**
     * Faktor auf die Dauer im Langzeitgedächtnis.
     *
     * @return Faktor größer 0 (1,0 = volle Dauer, kleiner = verblasst schneller)
     */
    public double langzeitFaktor() {
        return langzeitFaktor;
    }
}
