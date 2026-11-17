package blindspace.levels;

import blindspace.welt.Welt;

/**
 * Beschreibung eines spielbaren Levels: Aufbau der Welt und Startpose des Spielers.
 *
 * <p>
 * Jedes Level des Pakets {@code blindspace.levels} implementiert diese Schnittstelle. Das Spiel
 * ({@code blindspace.spiel.Spiel}) kennt nur diese Schnittstelle und kann dadurch jedes Level laden.
 * </p>
 */
public interface Level {

    /**
     * Name des Levels (für Anzeige und Tests).
     *
     * @return Anzeigename
     */
    String name();

    /**
     * Baut die Welt dieses Levels neu auf.
     *
     * @return eine neue, unabhängige {@link Welt}
     */
    Welt erzeuge();

    /**
     * Start-X-Position des Spielers.
     *
     * @return X-Koordinate in Metern
     */
    double startX();

    /**
     * Start-Y-Position des Spielers.
     *
     * @return Y-Koordinate in Metern
     */
    double startY();

    /**
     * Startausrichtung des Spielers.
     *
     * @return Winkel in Radiant (0 = nach +Y/Norden, positiv gegen den Uhrzeigersinn)
     */
    double startKurs();
}
