package blindspace.spieler;

/**
 * Eingabezustand des Spielers.
 *
 * <p>
 * Die Anzeige ({@code blindspace.anzeige.Ansicht}) schreibt Tastatur und Maus in diese Felder, der Spielkern liest
 * sie. Die Felder sind öffentlich und {@code volatile}, damit auch Selbsttests ohne Fenster Eingaben
 * simulieren können.
 * </p>
 */
public final class Eingabe {
    /** Vorwärts gehen gedrückt (W oder Pfeil hoch). */
    public volatile boolean vor;

    /** Rückwärts gehen gedrückt (S oder Pfeil runter). */
    public volatile boolean rueck;

    /** Nach links drehen gedrückt (A oder Pfeil links). */
    public volatile boolean links;

    /** Nach rechts drehen gedrückt (D oder Pfeil rechts). */
    public volatile boolean rechts;

    /** Gewünschte Körperstufe 0..3 oder -1 (keine Änderung); wird vom Körper verbraucht. */
    public volatile int stufeWunsch = -1;

    /** Mausposition horizontal im Mausbereich, 0 (links, Hände eng) bis 1 (rechts, Hände weit). */
    public volatile double mausU = 0.375;

    /** Mausposition vertikal im Mausbereich, 0 (unten) bis 1 (oben); bestimmt die Höhe im Tastband. */
    public volatile double mausV = 0.125;

    /** Linke Maustaste gedrückt: nach links tasten. */
    public volatile boolean mausLinks;

    /** Rechte Maustaste gedrückt: nach rechts tasten. */
    public volatile boolean mausRechts;

    /**
     * Ausrichten gewünscht (Leertaste oder Num 0); wird vom Spielkern verbraucht und gilt für genau einen
     * Simulationsschritt.
     */
    public volatile boolean ausrichten;

    /** Wurde die Maus schon bewegt? Davor bleiben die Hände halb ausgestreckt in der Startpose. */
    public volatile boolean mausBewegt;

    /** Erzeugt einen Eingabezustand im Ausgangszustand (nichts gedrückt, Hände in der Startposition). */
    public Eingabe() {
    }

    /** Setzt alle Eingaben auf den Ausgangszustand zurück (Neustart). */
    public void zuruecksetzen() {
        vor = rueck = links = rechts = false;
        stufeWunsch = -1;
        ausrichten = false;
        mausU = 0.375;
        mausV = 0.125;
        mausLinks = mausRechts = false;
        mausBewegt = false;
    }
}
