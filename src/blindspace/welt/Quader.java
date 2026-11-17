package blindspace.welt;

/**
 * Achsenparalleler Quader (AABB) der Spielwelt.
 *
 * <p>
 * Jedes Objekt der Welt (Wand, Möbel, Pfeiler) ist ein solcher Quader mit stabiler numerischer ID.
 * Jeder Quader hat eine Objektkategorie und eine Oberflächenart. Die Instanzen sind unveränderlich.
 * </p>
 */
public final class Quader {
    /** Stabile ID des Objekts (der Boden hat die reservierte ID {@link Welt#BODEN_ID}). */
    public final int id;

    /** Anzeigename, nur zur Orientierung (Debug, Tests). */
    public final String name;

    /** Objektkategorie (Wand, Möbel, ...). */
    public final Objektart art;

    /** Oberflächenart (rau, glatt, strukturiert). */
    public final Oberflaeche oberflaeche;

    /** Kleinste X-Koordinate. */
    public final double minX;

    /** Kleinste Y-Koordinate. */
    public final double minY;

    /** Kleinste Z-Koordinate. */
    public final double minZ;

    /** Größte X-Koordinate. */
    public final double maxX;

    /** Größte Y-Koordinate. */
    public final double maxY;

    /** Größte Z-Koordinate (Höhe des Objekts). */
    public final double maxZ;

    /**
     * Erzeugt einen Quader.
     *
     * @param id   stabile ID des Objekts
     * @param name Anzeigename
     * @param art  Objektkategorie
     * @param oberflaeche Oberflächenart
     * @param minX kleinste X-Koordinate
     * @param minY kleinste Y-Koordinate
     * @param minZ kleinste Z-Koordinate
     * @param maxX größte X-Koordinate
     * @param maxY größte Y-Koordinate
     * @param maxZ größte Z-Koordinate
     */
    public Quader(int id, String name, Objektart art, Oberflaeche oberflaeche, double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
        this.id = id;
        this.name = name;
        this.art = art;
        this.oberflaeche = oberflaeche;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
