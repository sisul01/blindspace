package blindspace.spieler;

import static blindspace.konfig.Konstanten.*;

import blindspace.welt.Welt;
import java.util.ArrayList;
import java.util.List;

/**
 * Manuelle Ausrichtung und Wandführung (Konzept Abschnitt 7).
 *
 * <p>
 * Tastereignisse werden in jedem Modus protokolliert. Ein gültiges Tastpaar liegt vor, wenn zwei Ereignisse
 * dieselbe Fläche desselben Objekts betreffen, 20 bis 80 cm auseinanderliegen und mindestens 0,4 s und höchstens
 * 20 s auseinander sind (gleichzeitige Berührungen beider Hände zählen also nicht). Dann beginnt die
 * Wandführung (die Zufallsdrift ist aus), und eine Ausrichtung ist für 20 s <em>möglich</em>. Ausgeführt wird sie
 * nur auf Wunsch des Spielers (Leertaste oder Num 0): seitliches Tasten richtet parallel zur Fläche aus, Tasten
 * nach vorn senkrecht.
 * </p>
 */
public final class Ausrichtung {

    /**
     * Ein protokolliertes Tastereignis.
     *
     * @param objektId   ID des ertasteten Objekts
     * @param achse      Achse der getroffenen Fläche (0 = X, 1 = Y, 2 = Z)
     * @param vorzeichen Vorzeichen der Flächennormalen auf dieser Achse
     * @param x          X-Koordinate des Tastpunkts
     * @param y          Y-Koordinate des Tastpunkts
     * @param z          Z-Koordinate des Tastpunkts
     * @param zeit       Spielzeit des Ereignisses
     * @param seitlich   true, wenn die Hand seitlich tastete (sonst nach vorn)
     */
    public record Tastereignis(int objektId, int achse, int vorzeichen, double x, double y, double z, double zeit,
            boolean seitlich) {
    }

    /**
     * Eine mögliche Ausrichtung, entstanden durch ein gültiges Tastpaar.
     *
     * @param achse      Achse der Fläche, nach der ausgerichtet wird
     * @param vorzeichen Vorzeichen der Flächennormalen
     * @param senkrecht  true: Front zur Fläche, false: parallel zur Fläche
     * @param erzeugt    Spielzeit des Tastpaars
     */
    private record Vormerkung(int achse, int vorzeichen, boolean senkrecht, double erzeugt) {
    }

    /** Protokoll der Tastereignisse der letzten 20 Sekunden. */
    private final List<Tastereignis> log = new ArrayList<>();

    /** Aktuelle Ausrichtungsmöglichkeit oder null. */
    private Vormerkung vormerkung;

    /** Anzahl ausgeführter Ausrichtungen (Drehung von mindestens 1 Grad). */
    private int anzahl;

    /** Wandführung aktiv (Drift aus). */
    private boolean wandfuehrung;

    /** Erzeugt eine Ausrichtung ohne Protokoll, ohne Möglichkeit und mit inaktiver Wandführung. */
    public Ausrichtung() {
    }

    /** Setzt Protokoll, Ausrichtungsmöglichkeit, Zähler und Wandführung zurück. */
    public void reset() {
        log.clear();
        vormerkung = null;
        anzahl = 0;
        wandfuehrung = false;
    }

    /**
     * Anzahl der ausgeführten Ausrichtungen.
     *
     * @return Zähler
     */
    public int anzahl() {
        return anzahl;
    }

    /**
     * Ist die Wandführung aktiv (Zufallsdrift ausgeschaltet)?
     *
     * @return true bei aktiver Wandführung
     */
    public boolean wandfuehrung() {
        return wandfuehrung;
    }

    /**
     * Ist gerade eine Ausrichtung möglich, das heißt: gibt es ein gültiges Tastpaar der letzten 20 Sekunden?
     *
     * @return true, wenn die Ausrichtungstaste jetzt wirkt
     */
    public boolean moeglich() {
        return vormerkung != null;
    }

    /**
     * Protokolliert ein Tastereignis. Bildet es mit einem früheren Ereignis ein gültiges Tastpaar, wird die
     * Ausrichtung möglich und die Wandführung beginnt. Nur senkrechte Flächen (Normale in X oder Y) zählen;
     * Oberseiten werden nie ausgerichtet. Die Ausrichtungsrichtung folgt dem neuesten Ereignis.
     *
     * @param e neues Tastereignis
     */
    public void ereignis(Tastereignis e) {
        log.removeIf(alt -> e.zeit() - alt.zeit() > AUSR_ZEITFENSTER);
        if (e.achse() != 2) {
            for (Tastereignis alt : log) {
                if (alt.objektId() != e.objektId() || alt.achse() != e.achse()
                        || alt.vorzeichen() != e.vorzeichen()) {
                    continue;
                }
                double d = Math.sqrt(Math.pow(alt.x() - e.x(), 2) + Math.pow(alt.y() - e.y(), 2)
                        + Math.pow(alt.z() - e.z(), 2));
                boolean abstandOk = d >= AUSR_ABSTAND_MIN && d <= AUSR_ABSTAND_MAX;
                boolean zeitOk = e.zeit() - alt.zeit() >= AUSR_ZEITABSTAND_MIN;
                if (abstandOk && zeitOk) {
                    // seitliches Tasten -> parallel, Tasten nach vorn -> senkrecht
                    vormerkung = new Vormerkung(e.achse(), e.vorzeichen(), !e.seitlich(), e.zeit());
                    wandfuehrung = true;
                    break;
                }
            }
        }
        log.add(e);
    }

    /**
     * Winkel einer Blickrichtung im Körper-Winkelsystem (0 = +Y, positiv gegen den Uhrzeigersinn).
     *
     * @param dx X-Anteil der Richtung
     * @param dy Y-Anteil der Richtung
     * @return Winkel in Radiant
     */
    private static double winkelVon(double dx, double dy) {
        return Math.atan2(-dx, dy);
    }

    /**
     * Ein Simulationsschritt: abgelaufene Ausrichtungsmöglichkeit verwerfen, auf Wunsch ausrichten und
     * prüfen, ob die Wandführung endet.
     *
     * <p>
     * Die Ausrichtung wird nur ausgeführt, wenn der Spieler sie wünscht, eine Möglichkeit besteht und keine
     * Drehanimation läuft. Die Möglichkeit bleibt danach bis zum Ablauf der 20 s bestehen, die Taste kann also
     * nach einer manuellen Drehung erneut ausrichten. Die Wandführung endet, wenn keine Hand Kontakt hat und
     * der horizontale Abstand zur nächsten Fläche im aktuellen Tastband größer als {@code LOCK_RELEASE_DIST} ist.
     * </p>
     *
     * @param zeit            aktuelle Spielzeit
     * @param k               Körper (wird bei der Ausrichtung gedreht)
     * @param welt            Welt für die Abstandsabfrage
     * @param handKontakt     true, wenn mindestens eine Hand gerade Kontakt hat
     * @param ausrichtenWunsch true, wenn der Spieler in diesem Schritt die Ausrichtungstaste gedrückt hat
     */
    public void update(double zeit, Koerper k, Welt welt, boolean handKontakt, boolean ausrichtenWunsch) {
        if (vormerkung != null && zeit - vormerkung.erzeugt() > AUSR_VORMERKUNG) {
            vormerkung = null;
        }
        if (ausrichtenWunsch && vormerkung != null && !k.drehAktiv()) {
            fuehreAus(k);
        }
        if (wandfuehrung && !handKontakt) {
            double lo = bandUnten(k.gehen(), k.stufe()), hi = bandOben(k.gehen(), k.stufe());
            if (welt.naechsterAbstand(k.x, k.y, lo, hi) > LOCK_RELEASE_DIST) {
                wandfuehrung = false;
            }
        }
    }

    /**
     * Führt die Ausrichtung aus: berechnet die Zielrichtung (senkrecht oder parallel mit dem kleineren
     * Drehwinkel), dreht den Körper animiert, zählt die Ausrichtung (ab 1 Grad Drehung) und schaltet die
     * Wandführung ein.
     *
     * @param k Körper, der ausgerichtet wird
     */
    private void fuehreAus(Koerper k) {
        double nx = vormerkung.achse() == 0 ? vormerkung.vorzeichen() : 0;
        double ny = vormerkung.achse() == 1 ? vormerkung.vorzeichen() : 0;
        double ziel;
        if (vormerkung.senkrecht()) {
            ziel = winkelVon(-nx, -ny); // Front zur Fläche
        } else {
            double a1 = winkelVon(-ny, nx);
            double a2 = winkelVon(ny, -nx);
            double d1 = Math.abs(Koerper.normalisiere(a1 - k.kurs));
            double d2 = Math.abs(Koerper.normalisiere(a2 - k.kurs));
            ziel = d1 <= d2 ? a1 : a2;
        }
        double delta = Koerper.normalisiere(ziel - k.kurs);
        if (Math.abs(delta) >= AUSR_MIN_WINKEL) {
            k.drehe(delta);
            anzahl++;
        }
        wandfuehrung = true;
    }
}
