package blindspace.welt;

/**
 * Ergebnis einer Tastprobe: Welches Objekt wurde an welcher Stelle und auf welcher Fläche berührt?
 *
 * @param objektId  ID des getroffenen Quaders ({@link Welt#BODEN_ID} für den Boden)
 * @param achse     Achse der getroffenen Fläche: 0 = X, 1 = Y, 2 = Z
 * @param vorzeichen Vorzeichen der Flächennormalen auf dieser Achse (+1 oder -1)
 * @param px        X-Koordinate des Berührpunkts auf der Oberfläche
 * @param py        Y-Koordinate des Berührpunkts auf der Oberfläche
 * @param pz        Z-Koordinate des Berührpunkts auf der Oberfläche
 * @param abstand   Abstand der Probe zur Oberfläche (0, wenn die Probe im Objekt liegt)
 * @param oberflaeche Oberflächenart des getroffenen Objekts
 * @param art       Objektkategorie des getroffenen Objekts
 */
public record Beruehrung(int objektId, int achse, int vorzeichen, double px, double py, double pz,
        double abstand, Oberflaeche oberflaeche, Objektart art) {

    /**
     * X-Anteil der Flächennormalen.
     *
     * @return {@code vorzeichen}, wenn die Fläche auf der X-Achse liegt, sonst 0
     */
    public double nx() {
        return achse == 0 ? vorzeichen : 0;
    }

    /**
     * Y-Anteil der Flächennormalen.
     *
     * @return {@code vorzeichen}, wenn die Fläche auf der Y-Achse liegt, sonst 0
     */
    public double ny() {
        return achse == 1 ? vorzeichen : 0;
    }

    /**
     * Z-Anteil der Flächennormalen.
     *
     * @return {@code vorzeichen}, wenn die Fläche auf der Z-Achse liegt, sonst 0
     */
    public double nz() {
        return achse == 2 ? vorzeichen : 0;
    }
}
