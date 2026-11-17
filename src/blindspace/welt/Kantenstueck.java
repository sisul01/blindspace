package blindspace.welt;

/**
 * Ein Stück einer Quaderkante: die Strecke, die bei einer Berührung nahe der Kante sichtbar wird.
 *
 * <p>
 * Die Kante eines Quaders wird in Stücke fester Länge ab ihrem Anfang eingeteilt. Ein Stück ist durch Objekt,
 * Kantenachse, Lage der Kante und Stücknummer eindeutig bestimmt; alle Koordinaten ergeben sich daraus. Zwei
 * Berührungen im selben Stück liefern deshalb gleiche Werte (gleiche Schlüssel für die Verwaltung).
 * </p>
 *
 * @param objektId    ID des Quaders, zu dem die Kante gehört
 * @param achse       Richtung der Kante: 0 = X, 1 = Y, 2 = Z
 * @param seiteA      Lage in der ersten Querachse (Achse {@code (achse + 1) % 3}): 0 = Minimum, 1 = Maximum
 * @param seiteB      Lage in der zweiten Querachse (Achse {@code (achse + 2) % 3}): 0 = Minimum, 1 = Maximum
 * @param zelle       Nummer des Stücks entlang der Kante (0 = am Kantenanfang)
 * @param x0          X-Koordinate des Stückanfangs
 * @param y0          Y-Koordinate des Stückanfangs
 * @param z0          Z-Koordinate des Stückanfangs
 * @param x1          X-Koordinate des Stückendes
 * @param y1          Y-Koordinate des Stückendes
 * @param z1          Z-Koordinate des Stückendes
 * @param oberflaeche Oberflächenart des Quaders (bestimmt die Dauer im Langzeitgedächtnis)
 */
public record Kantenstueck(int objektId, int achse, int seiteA, int seiteB, int zelle, double x0, double y0,
        double z0, double x1, double y1, double z1, Oberflaeche oberflaeche) {
}
