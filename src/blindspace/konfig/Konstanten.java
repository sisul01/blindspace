package blindspace.konfig;

/**
 * Zentrale Konstanten des Spiels (Balancing, Konzept Abschnitt 12).
 *
 * <p>
 * Alle Werte sind hier gesammelt, damit sie ohne Eingriff in die Spiellogik angepasst werden können.
 * Einheiten: Längen in Metern, Zeiten in Sekunden, Winkel in Radiant, Helligkeit als Anteil 0..1.
 * Koordinatensystem: X = rechts, Y = vorwärts (Norden), Z = hoch.
 * </p>
 */
public final class Konstanten {

    /** Reine Konstantenklasse, keine Instanzen. */
    private Konstanten() {
    }

    // ===== Simulation =====

    /** Fester Zeitschritt der Simulation (120 Schritte pro Sekunde). */
    public static final double SIM_DT = 1.0 / 120.0;

    /** Obergrenze für die pro Bild simulierte Zeit; verhindert große Sprünge nach Verzögerungen. */
    public static final double MAX_FRAME_DT = 0.05;

    // ===== Körper (Konzept Abschnitt 3) =====

    /** Durchmesser des Beinzylinders (entspricht der Schrittlänge, der Fuß berührt Hindernisse zuerst). */
    public static final double BEIN_DURCHMESSER = 0.80;

    /** Radius des Beinzylinders. */
    public static final double BEIN_RADIUS = BEIN_DURCHMESSER / 2;

    /** Höhe des Beinzylinders in den vier Körperstufen (Tasten 1 bis 4): aufrecht bis geduckt. */
    public static final double[] BEIN_HOEHEN = { 0.90, 0.70, 0.50, 0.30 };

    /** Breite (X) des Oberkörper-Quaders. */
    public static final double TORSO_BREITE = 0.40;

    /** Tiefe (Y) des Oberkörper-Quaders. */
    public static final double TORSO_TIEFE = 0.30;

    /** Höhe (Z) des Oberkörper-Quaders. */
    public static final double TORSO_HOEHE = 0.60;

    /** Durchmesser des Kopfzylinders. */
    public static final double KOPF_DURCHMESSER = 0.20;

    /** Radius des Kopfzylinders. */
    public static final double KOPF_RADIUS = KOPF_DURCHMESSER / 2;

    /** Höhe des Kopfzylinders. */
    public static final double KOPF_HOEHE = 0.20;

    /** Augenhöhe über der Beinhöhe (Kopfmitte): Oberkörperhöhe plus halbe Kopfhöhe. */
    public static final double AUGEN_OFFSET = TORSO_HOEHE + KOPF_HOEHE / 2;

    /** Schulterhöhe über der Beinhöhe (knapp unter der Oberkante des Oberkörpers). */
    public static final double SCHULTER_OFFSET = TORSO_HOEHE - 0.05;

    // ===== Tastbänder (Z-Bereich der Hände, Konzept Abschnitt 4.3) =====

    /** Tastband beim Gehen (nur aufrecht erlaubt): {unten, oben}. */
    public static final double[] GEHEN_BAND = { 1.30, 1.60 };

    /**
     * Tiefste Höhe, die eine Hand ertasten kann (knapp über dem Boden). Damit sind niedrige Stufen ertastbar,
     * der Boden selbst und flache Gegenstände unter dieser Höhe aber nicht.
     */
    public static final double TAST_MIN_HOEHE = 0.05;

    /** Tastband im Stehen je Körperstufe: {unten, oben}; das tiefste Band beginnt bei {@link #TAST_MIN_HOEHE}. */
    public static final double[][] STEHEN_BAENDER = { { 1.40, 1.80 }, { 1.00, 1.40 }, { 0.60, 1.00 },
            { TAST_MIN_HOEHE, 0.60 } };

    /**
     * Untere Grenze des Tastbands.
     *
     * @param gehen true im Gehen-Modus
     * @param stufe Körperstufe 0 (aufrecht) bis 3 (geduckt)
     * @return untere Z-Grenze in Metern; Gehen gilt nur bei Stufe 0, sonst das Stehband
     */
    public static double bandUnten(boolean gehen, int stufe) {
        return (gehen && stufe == 0) ? GEHEN_BAND[0] : STEHEN_BAENDER[stufe][0];
    }

    /**
     * Obere Grenze des Tastbands.
     *
     * @param gehen true im Gehen-Modus
     * @param stufe Körperstufe 0 (aufrecht) bis 3 (geduckt)
     * @return obere Z-Grenze in Metern; Gehen gilt nur bei Stufe 0, sonst das Stehband
     */
    public static double bandOben(boolean gehen, int stufe) {
        return (gehen && stufe == 0) ? GEHEN_BAND[1] : STEHEN_BAENDER[stufe][1];
    }

    // ===== Tasten und Tastpunkte (Konzept Abschnitte 4.4 und 8) =====

    /** Mindestzeit zwischen zwei Tastpunkten derselben Hand. */
    public static final double TASTINTERVALL = 0.5;

    /** Suchradius der Hand beim Gehen (grob). */
    public static final double SUCHRADIUS_GEHEN = 0.07;

    /** Suchradius der Hand im Stehen (präzise). */
    public static final double SUCHRADIUS_STEHEN = 0.03;

    /** Durchmesser der Tastpunkt-Visualisierung beim Gehen. */
    public static final double PUNKT_DURCHMESSER_GEHEN = 0.10;

    /** Durchmesser der Tastpunkt-Visualisierung im Stehen. */
    public static final double PUNKT_DURCHMESSER_STEHEN = 0.20;

    /** Starthelligkeit eines Tastpunkts beim Gehen. */
    public static final double HELLIGKEIT_GEHEN = 0.5;

    /** Starthelligkeit eines Tastpunkts im Stehen. */
    public static final double HELLIGKEIT_STEHEN = 1.0;

    // ===== Kanten (Konzept Abschnitt 8.5) =====

    /**
     * Größter Abstand eines Berührpunkts zu einer Quaderkante, bei dem die Kante als ertastet gilt. Der Punkt
     * muss dazu in beiden Achsen, die senkrecht zur Kante stehen, höchstens so weit von der Quaderbegrenzung
     * entfernt sein.
     */
    public static final double KANTE_TOLERANZ = 0.05;

    /**
     * Länge eines Kantenstücks entlang der Kante (Raster ab dem Kantenanfang). Mit 40 cm ist ein Stück doppelt so
     * lang wie ein Tastpunkt im Stehen, damit es als Linie erkennbar ist und benachbarte Stücke zu einer
     * durchgehenden Kante zusammenwachsen.
     */
    public static final double KANTE_LAENGE = 0.40;

    /** Starthelligkeit eines Kantenstücks beim Gehen (höher als ein Tastpunkt, damit Linien lesbar bleiben). */
    public static final double KANTE_HELLIGKEIT_GEHEN = 0.8;

    /** Starthelligkeit eines Kantenstücks im Stehen. */
    public static final double KANTE_HELLIGKEIT_STEHEN = 1.0;

    /**
     * Mindestzeit, bis ein bereits sichtbares Kantenstück durch erneute Berührung aufgefrischt wird (verhindert,
     * dass bei jedem Simulationsschritt ein neues Objekt entsteht).
     */
    public static final double KANTE_ERNEUERUNG = 0.1;

    /** Linienbreite einer Kante in Metern (perspektivisch skaliert). */
    public static final double KANTE_BREITE = 0.025;

    /** Kleinste Linienbreite einer Kante auf dem Bildschirm in Pixeln. */
    public static final double KANTE_MIN_PIXEL = 4.0;

    // ===== Gedächtnis (Konzept Abschnitt 8) =====

    /** Dauer des Kurzzeitgedächtnisses für Punkte, die beim Gehen entstanden sind (Verblassen bis zum Restwert). */
    public static final double KZ_DAUER_GEHEN = 15.0;

    /** Dauer des Kurzzeitgedächtnisses für Punkte, die im Stehen entstanden sind. */
    public static final double KZ_DAUER_STEHEN = 30.0;

    /** Restanteil der Starthelligkeit am Ende des Kurzzeitgedächtnisses (2 bis 3 %). */
    public static final double KZ_REST = 0.025;

    /** Obergrenze gleichzeitig aktiver Kurzzeitpunkte; überzählige gehen ins Langzeitgedächtnis über. */
    public static final int MAX_TASTPUNKTE = 220;

    /** Lebensdauer einer Verblassungsgruppe im Langzeitgedächtnis (Mittelwert aus 10 bis 15 Minuten). */
    public static final double LZ_DAUER = 12 * 60.0;

    /** Kantenlänge der Zellen, in denen Langzeitpunkte zu Verblassungsgruppen zusammengefasst werden. */
    public static final double LZ_ZELLE = 0.25;

    /** Faktor auf die Langzeitdauer strukturierter Oberflächen (Strukturen verblassen schneller). */
    public static final double LZ_FAKTOR_STRUKTURIERT = 0.5;

    /** Zusätzliche Aufweitung einer Gruppe bis zum Verschwinden (0,0 = keine, 1,0 = doppelter Radius). */
    public static final double LZ_UNSCHAERFE = 1.0;

    // ===== Kollisionsfaktoren (Helligkeit aller aktiven Visualisierungen nach einer Körperkollision) =====

    /** Faktor bei einer Kollision mit den Beinen. */
    public static final double KOLLISION_FAKTOR_BEIN = 0.70;

    /** Faktor bei einer Kollision mit dem Oberkörper (Rumpf). */
    public static final double KOLLISION_FAKTOR_RUMPF = 0.55;

    /** Faktor bei einer Kollision mit dem Kopf. */
    public static final double KOLLISION_FAKTOR_KOPF = 0.40;

    // ===== Hände (Konzept Abschnitt 4) =====

    /** Minimale Armlänge (horizontal gemessen). */
    public static final double ARM_MIN = 0.25;

    /** Maximale Armlänge (horizontal gemessen); ohne Objektkontakt voll ausgestreckt. */
    public static final double ARM_MAX = 0.65;

    /** Seitlicher Abstand der Schultern von der Körpermitte. */
    public static final double SCHULTER_X = TORSO_BREITE / 2;

    /** Lage der Schultern nach vorn: Vorderkante des Oberkörpers. */
    public static final double SCHULTER_Y = TORSO_TIEFE / 2;

    /** Winkel des seitlich tastenden Arms von der Vorwärtsrichtung nach außen (hält die Hand im Sichtfeld). */
    public static final double SEITEN_WINKEL = Math.toRadians(60);

    /** Kleinste Spannweite (seitlicher Abstand je Hand von der Mitte) beim Vorwärtstasten. */
    public static final double SPANN_MIN = 0.10;

    /** Größte Spannweite (seitlicher Abstand je Hand von der Mitte) beim Vorwärtstasten. */
    public static final double SPANN_MAX = 0.50;

    /** Geschwindigkeit der Handbewegung in m/s. */
    public static final double HAND_GESCHW = 1.6;

    /** Kleinster Abstand, den eine gleitende Hand zu einer Fläche hält. */
    public static final double HAND_BLOCK_RADIUS = 0.02;

    /** Ein bestehender (klebender oder gleitender) Kontakt bleibt bis zu diesem Abstand erhalten. */
    public static final double HALTE_RADIUS = 0.08;

    /** Abweichung der Zielposition (seitlich oder von der Fläche weg), ab der sich eine klebende Hand löst. */
    public static final double HAND_LOESEN = 0.10;

    /** Länge des dargestellten Handellipsoids. */
    public static final double HAND_LAENGE = 0.20;

    /** Breite des dargestellten Handellipsoids. */
    public static final double HAND_BREITE = 0.08;

    /** Spannweite je Hand in der Startpose. */
    public static final double START_SPANN = 0.25;

    /** Armlänge in der Startpose (halb ausgestreckt) und bis zur ersten Mausbewegung. */
    public static final double START_AUSSTRECKUNG = 0.45;

    /** Handhöhe in der Startpose (Brusthöhe). */
    public static final double START_Z = 1.45;

    /** Anteil des Fensters (zentral), der den gesamten Bewegungsbereich der Maus abdeckt. */
    public static final double MAUS_BEREICH = 0.6;

    // ===== Bewegung (Konzept Abschnitt 5) =====

    /** Länge eines Schritts. */
    public static final double SCHRITTLAENGE = 0.50;

    /** Gehgeschwindigkeit in m/s. */
    public static final double GEHGESCHW = 1.1;

    /** Anzahl Schritte ohne Drift; ab dem nächsten Schritt setzt die Zufallsdrift ein. */
    public static final int DRIFT_START_SCHRITTE = 10;

    /** Kleinste Winkelabweichung pro Schritt durch die Drift. */
    public static final double DRIFT_MIN = Math.toRadians(1.0);

    /** Größte Winkelabweichung pro Schritt durch die Drift. */
    public static final double DRIFT_MAX = Math.toRadians(2.0);

    /** Länge eines Einzelschritts (kurzer Tastendruck): ein halber Schritt. */
    public static final double HALBSCHRITT = SCHRITTLAENGE / 2;

    /** Länge eines Einzelschritts in geduckter Haltung (Körperstufen 2 bis 4), vorwärts und rückwärts. */
    public static final double DUCK_SCHRITT = 0.10;

    /** Nominelle Größe eines Drehschritts. */
    public static final double DREH_SCHRITT = Math.toRadians(15.0);

    /** Zufällige Abweichung eines Drehschritts (plus/minus). */
    public static final double DREH_ZUFALL = Math.toRadians(1.0);

    /** Dauer der Drehanimation. */
    public static final double DREH_DAUER = 0.35;

    /** Nachlaufzeit des Gehen-Modus nach dem letzten Schritt (verhindert Flackern). */
    public static final double GEHEN_NACHLAUF = 0.3;

    // ===== Kollisionen (Konzept Abschnitt 6) =====

    /** Rückstoß nach einer Körperkollision: ein halber Schritt. */
    public static final double RUECKSTOSS = 0.25;

    /** Dauer der Eingabesperre (Gehen und Drehen) nach einer Körperkollision. */
    public static final double EINGABESPERRE = 0.6;

    // ===== Manuelle Ausrichtung (Konzept Abschnitt 7) =====

    /** Kleinster Abstand zweier Tastpunkte, damit eine Ausrichtung möglich wird. */
    public static final double AUSR_ABSTAND_MIN = 0.20;

    /** Größter Abstand zweier Tastpunkte, damit eine Ausrichtung möglich wird. */
    public static final double AUSR_ABSTAND_MAX = 0.80;

    /** Größter zeitlicher Abstand zweier Tastereignisse für eine Ausrichtung. */
    public static final double AUSR_ZEITFENSTER = 20.0;

    /** Kleinster zeitlicher Abstand zweier Tastereignisse; gleichzeitige Berührungen beider Hände zählen nicht. */
    public static final double AUSR_ZEITABSTAND_MIN = 0.4;

    /** Gültigkeitsdauer der Ausrichtungsmöglichkeit (Vormerkung) nach einem gültigen Tastpaar. */
    public static final double AUSR_VORMERKUNG = 20.0;

    /** Kleinster Drehwinkel, der als Ausrichtung gezählt wird. */
    public static final double AUSR_MIN_WINKEL = Math.toRadians(1.0);

    /**
     * Größte Winkelabweichung zwischen Blickrichtung und Wandrichtung, bei der beim Loslaufen mit seitlichem
     * Wandkontakt automatisch parallel ausgerichtet wird (Konzept Abschnitt 7.5).
     */
    public static final double AUSR_AUTO_MAX_WINKEL = Math.toRadians(20.0);

    /** Horizontaler Abstand zur nächsten Fläche, ab dem die Wandführung endet (sofern kein Handkontakt). */
    public static final double LOCK_RELEASE_DIST = 1.0;

    // ===== Darstellung =====

    /** Horizontales Sichtfeld der Kamera. */
    public static final double SICHTFELD = Math.toRadians(120.0);
}
