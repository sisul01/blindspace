package blindspace.levels;

import static blindspace.welt.Objektart.*;
import static blindspace.welt.Oberflaeche.*;

import blindspace.welt.Welt;

/**
 * Das Testlevel des Prototyps (Konzept Abschnitt 9): eine Halle von 12 x 10 m mit zwei Räumen.
 *
 * <p>
 * Aufbau (Innenmaße x = -6..6, y = -5..5, Wandhöhe 2,6 m):
 * </p>
 * <ul>
 * <li>Trennwand bei y = 0,5..0,7 mit Durchgang bei x = -1..1.</li>
 * <li>Ausgang: Lücke in der Nordwand bei x = 3,5..4,5. Der Ausgang ist bewusst kein Objekt, sondern die
 * Abwesenheit von Geometrie.</li>
 * <li>Raum 1 (Süd, Start bei (-4,5 | -3,5), Blick nach Norden): Regal (2,0 m), Tisch (0,75 m), Kiste (0,5 m).</li>
 * <li>Raum 2 (Nord): Sockel (0,75 m), Pfeiler (1,6 m).</li>
 * </ul>
 *
 * <p>
 * Die Klasse ist in Teilmethoden gegliedert ({@link #aussenwaende}, {@link #trennwand}, {@link #raum1},
 * {@link #raum2}), damit sie später leicht erweitert werden kann.
 * </p>
 */
public final class Testlevel implements Level {

    /** Höhe aller Wände. */
    private static final double WANDHOEHE = 2.6;

    /** Dicke aller Wände. */
    private static final double WANDDICKE = 0.2;

    /** Erzeugt das Testlevel; die Welt entsteht erst in {@link #erzeuge()}. */
    public Testlevel() {
    }

    @Override
    public String name() {
        return "Testlevel";
    }

    @Override
    public double startX() {
        return -4.5;
    }

    @Override
    public double startY() {
        return -3.5;
    }

    @Override
    public double startKurs() {
        return 0.0;
    }

    @Override
    public Welt erzeuge() {
        Welt w = new Welt();
        aussenwaende(w);
        trennwand(w);
        raum1(w);
        raum2(w);
        return w;
    }

    /**
     * Baut die vier Außenwände; die Nordwand hat eine Lücke (der Ausgang) bei x = 3,5..4,5.
     *
     * @param w Welt, in die gebaut wird
     */
    private static void aussenwaende(Welt w) {
        double d = WANDDICKE;
        w.add("Westwand", WAND, RAU, -6.0 - d, -5.0 - d, -6.0, 5.0 + d, WANDHOEHE);
        w.add("Ostwand", WAND, RAU, 6.0, -5.0 - d, 6.0 + d, 5.0 + d, WANDHOEHE);
        w.add("Suedwand", WAND, RAU, -6.0 - d, -5.0 - d, 6.0 + d, -5.0, WANDHOEHE);
        w.add("Nordwand West", WAND, RAU, -6.0 - d, 5.0, 3.5, 5.0 + d, WANDHOEHE);
        w.add("Nordwand Ost", WAND, RAU, 4.5, 5.0, 6.0 + d, 5.0 + d, WANDHOEHE);
    }

    /**
     * Baut die Trennwand zwischen den Räumen mit einem Durchgang bei x = -1..1.
     *
     * @param w Welt, in die gebaut wird
     */
    private static void trennwand(Welt w) {
        w.add("Trennwand West", WAND, RAU, -6.0, 0.5, -1.0, 0.7, WANDHOEHE);
        w.add("Trennwand Ost", WAND, RAU, 1.0, 0.5, 6.0, 0.7, WANDHOEHE);
    }

    /**
     * Baut Raum 1 (Süd): Regal an der Westwand, Tisch und Kiste.
     *
     * @param w Welt, in die gebaut wird
     */
    private static void raum1(Welt w) {
        w.add("Regal", MOEBEL, STRUKTURIERT, -6.0, -3.0, -5.6, -1.2, 2.0);
        w.add("Tisch", MOEBEL, STRUKTURIERT, -0.6, -3.0, 0.6, -2.2, 0.75);
        w.add("Kiste", MOEBEL, GLATT, 3.0, -4.0, 3.5, -3.5, 0.5);
    }

    /**
     * Baut Raum 2 (Nord): Sockel und Pfeiler.
     *
     * @param w Welt, in die gebaut wird
     */
    private static void raum2(Welt w) {
        w.add("Sockel", SONSTIGES, RAU, -3.0, 2.5, -2.4, 3.1, 0.75);
        w.add("Pfeiler", SAEULE, GLATT, 1.0, 2.0, 1.4, 2.4, 1.6);
    }
}
