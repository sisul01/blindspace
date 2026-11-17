package blindspace.spieler;

import static blindspace.konfig.Konstanten.*;

import blindspace.welt.Beruehrung;
import blindspace.welt.Welt;

/**
 * Die beiden synchron gespiegelten Hände (Konzept Abschnitt 4).
 *
 * <p>
 * Positionen werden in Körperkoordinaten gespeichert (x = rechts, y = vorwärts, z = hoch) und drehen daher
 * immer exakt mit dem Körper mit. Die Hände folgen einer Zielposition, die aus der Mauseingabe entsteht, und
 * stoppen bei Kontakt. Klebt eine Hand an einer Fläche, steht auch die andere still (synchroner Stopp). Die
 * klebende Hand löst sich erst, wenn das Ziel um {@code HAND_LOESEN} seitlich oder von der Fläche weg
 * abweicht, und gleitet dann an der Fläche zum neuen Ziel. Die Hände tasten nie den Boden selbst; ertastbar
 * ist nur, was im Tastband (ab {@code TAST_MIN_HOEHE}) liegt.
 * </p>
 */
public final class Haende {

    /** Zustand einer einzelnen Hand. */
    public static final class Hand {
        /** true für die linke, false für die rechte Hand. */
        public final boolean istLinks;

        /** X-Position in Körperkoordinaten (rechts positiv). */
        public double bx;

        /** Y-Position in Körperkoordinaten (vorwärts positiv). */
        public double by;

        /** Z-Position (Höhe über dem Boden). */
        public double bz;

        /** Hand klebt an einer Kontaktstelle. */
        public boolean haelt;

        /** Hand gleitet nach dem Lösen an der Fläche entlang zum Ziel. */
        public boolean gleitet;

        /** Diese Hand tastet gerade seitlich (sonst nach vorn). */
        public boolean seitlich;

        /** X-Position des Ankers (Körperkoordinaten) des klebenden Kontakts. */
        public double ax;

        /** Y-Position des Ankers (Körperkoordinaten) des klebenden Kontakts. */
        public double ay;

        /** Z-Position des Ankers (Körperkoordinaten) des klebenden Kontakts. */
        public double az;

        /** X-Anteil der Flächennormalen des Kontakts (Körperkoordinaten). */
        public double nbx;

        /** Y-Anteil der Flächennormalen des Kontakts (Körperkoordinaten). */
        public double nby;

        /** Z-Anteil der Flächennormalen des Kontakts (Körperkoordinaten). */
        public double nbz;

        /** Aktueller Kontakt (null = kein Kontakt). */
        public Beruehrung kontakt;

        /** Im letzten Update neu entstandener Kontakt. */
        public boolean neuerKontakt;

        /** Restzeit bis zum nächsten erlaubten Tastpunkt dieser Hand. */
        public double tastTimer;

        /**
         * Erzeugt eine Hand.
         *
         * @param links true für die linke Hand
         */
        Hand(boolean links) {
            this.istLinks = links;
        }

        /** Setzt die Hand in die Startpose (halb ausgestreckt auf Brusthöhe) und löscht den Kontaktzustand. */
        void setzeStartpose() {
            double vz = istLinks ? -1 : 1;
            bx = vz * START_SPANN;
            by = SCHULTER_Y + START_AUSSTRECKUNG;
            bz = START_Z;
            haelt = false;
            gleitet = false;
            seitlich = false;
            kontakt = null;
            neuerKontakt = false;
            tastTimer = 0;
        }
    }

    /** Die linke Hand. */
    public final Hand links = new Hand(true);

    /** Die rechte Hand. */
    public final Hand rechts = new Hand(false);

    /** Erzeugt beide Hände in der Startpose. */
    public Haende() {
        reset();
    }

    /** Setzt beide Hände in die Startpose zurück. */
    public void reset() {
        links.setzeStartpose();
        rechts.setzeStartpose();
    }

    /**
     * Beide Hände zum bequemen Durchlaufen.
     *
     * @return Feld {links, rechts}
     */
    public Hand[] alle() {
        return new Hand[] { links, rechts };
    }

    // ===== Koordinatentransformation Körper <-> Welt =====

    /**
     * Rechnet die Körperkoordinaten einer Hand in Weltkoordinaten um (Drehung um die Blickrichtung plus
     * Körperposition).
     *
     * @param k Körper (Position und Blickrichtung)
     * @param h Hand
     * @return Weltposition {x, y, z}
     */
    public static double[] weltPos(Koerper k, Hand h) {
        double c = Math.cos(k.kurs), s = Math.sin(k.kurs);
        return new double[] { k.x + h.bx * c - h.by * s, k.y + h.bx * s + h.by * c, h.bz };
    }

    /**
     * Rechnet eine Weltposition in die Körperkoordinaten der Hand zurück und speichert sie dort.
     *
     * @param k Körper (Position und Blickrichtung)
     * @param h Hand, deren Körperkoordinaten gesetzt werden
     * @param w Weltposition {x, y, z}
     */
    private static void ausWelt(Koerper k, Hand h, double[] w) {
        double c = Math.cos(k.kurs), s = Math.sin(k.kurs);
        double dx = w[0] - k.x, dy = w[1] - k.y;
        h.bx = dx * c + dy * s;
        h.by = -dx * s + dy * c;
        h.bz = w[2];
    }

    // ===== Zielpositionen aus der Mauseingabe =====

    /**
     * Berechnet die Zielpositionen beider Hände in Körperkoordinaten und setzt die Seitlich-Markierungen.
     *
     * <p>
     * Ohne Maustaste strecken sich beide Hände nach vorn (gespiegelt, Spannweite aus der Maus). Mit gedrückter
     * linker (rechter) Maustaste tastet die linke (rechte) Hand seitlich, der andere Arm bleibt vorn. Bis zur
     * ersten Mausbewegung sind die Arme nur halb ausgestreckt.
     * </p>
     *
     * @param in    Eingabezustand
     * @param gehen Gehen-Modus (bestimmt das Tastband)
     * @param stufe Körperstufe (bestimmt das Tastband)
     * @param zl    Ausgabe: Ziel der linken Hand {x, y, z}
     * @param zr    Ausgabe: Ziel der rechten Hand {x, y, z}
     */
    private void berechneZiele(Eingabe in, boolean gehen, int stufe, double[] zl, double[] zr) {
        double z = bandUnten(gehen, stufe) + in.mausV * (bandOben(gehen, stufe) - bandUnten(gehen, stufe));
        double spann = SPANN_MIN + in.mausU * (SPANN_MAX - SPANN_MIN);
        double ausstreckung = in.mausBewegt ? ARM_MAX : START_AUSSTRECKUNG;
        double vornY = SCHULTER_Y + ausstreckung;
        zl[0] = -spann;
        zl[1] = vornY;
        zl[2] = z;
        zr[0] = spann;
        zr[1] = vornY;
        zr[2] = z;
        links.seitlich = false;
        rechts.seitlich = false;
        double seitX = SCHULTER_X + ARM_MAX * Math.sin(SEITEN_WINKEL);
        double seitY = SCHULTER_Y + ARM_MAX * Math.cos(SEITEN_WINKEL);
        if (in.mausLinks && !in.mausRechts) {
            zl[0] = -seitX;
            zl[1] = seitY;
            links.seitlich = true;
        } else if (in.mausRechts && !in.mausLinks) {
            zr[0] = seitX;
            zr[1] = seitY;
            rechts.seitlich = true;
        }
    }

    // ===== Update =====

    /**
     * Ein Simulationsschritt der Hände: Ziele bestimmen, Kontakte prüfen, klebende Hände ggf. lösen, freie
     * Hände bewegen (bei klebender Hand steht auch die andere still) und den Kontaktstatus für die Tastpunkte
     * aktualisieren.
     *
     * @param dt    Zeitschritt in Sekunden
     * @param k     Körper (Position, Blickrichtung, Modus)
     * @param welt  Welt für die Tastproben
     * @param in    Eingabezustand (Mausposition und -tasten)
     */
    public void update(double dt, Koerper k, Welt welt, Eingabe in) {
        boolean gehen = k.gehen();
        double radius = gehen ? SUCHRADIUS_GEHEN : SUCHRADIUS_STEHEN;
        double[] zl = new double[3], zr = new double[3];
        berechneZiele(in, gehen, k.stufe(), zl, zr);

        for (Hand h : alle()) {
            h.neuerKontakt = false;
            double[] w = weltPos(k, h);
            if (h.haelt) {
                Beruehrung b = welt.sondiere(w[0], w[1], w[2], HALTE_RADIUS, false);
                if (b == null) {
                    h.haelt = false;
                    h.gleitet = false;
                } else if (loest(h, h.istLinks ? zl : zr)) {
                    h.haelt = false;
                    h.gleitet = true;
                }
            } else if (!h.gleitet) {
                // Kontakt durch Körperbewegung entstanden
                Beruehrung b = welt.sondiere(w[0], w[1], w[2], radius, false);
                if (b != null) {
                    festhalten(k, h, b);
                    h.neuerKontakt = true;
                }
            }
        }

        // Synchroner Stopp: klebt eine Hand, steht auch die andere still.
        if (!links.haelt && !rechts.haelt) {
            bewege(dt, k, welt, zl, zr, radius);
        }

        // Kontaktstatus für Tastpunkte
        for (Hand h : alle()) {
            double[] w = weltPos(k, h);
            h.kontakt = welt.sondiere(w[0], w[1], w[2], (h.haelt || h.gleitet) ? HALTE_RADIUS : radius, false);
        }
    }

    /**
     * Lässt eine Hand an ihrer aktuellen Position kleben: Anker und Flächennormale (in Körperkoordinaten)
     * werden gespeichert.
     *
     * @param k Körper (für die Umrechnung der Normalen in Körperkoordinaten)
     * @param h Hand
     * @param b Berührung, die zum Kleben führt
     */
    private void festhalten(Koerper k, Hand h, Beruehrung b) {
        h.haelt = true;
        h.gleitet = false;
        h.ax = h.bx;
        h.ay = h.by;
        h.az = h.bz;
        double c = Math.cos(k.kurs), s = Math.sin(k.kurs);
        h.nbx = b.nx() * c + b.ny() * s;
        h.nby = -b.nx() * s + b.ny() * c;
        h.nbz = b.nz();
    }

    /**
     * Prüft, ob sich eine klebende Hand löst: Das Ziel muss seitlich zur Fläche oder von der Fläche weg um
     * mindestens {@code HAND_LOESEN} vom Anker abweichen. Bewegung in das Objekt hinein löst nicht.
     *
     * @param h    klebende Hand
     * @param ziel Zielposition der Hand {x, y, z} in Körperkoordinaten
     * @return true, wenn sich die Hand löst
     */
    private boolean loest(Hand h, double[] ziel) {
        double dx = ziel[0] - h.ax, dy = ziel[1] - h.ay, dz = ziel[2] - h.az;
        double dn = dx * h.nbx + dy * h.nby + dz * h.nbz;
        double tx = dx - dn * h.nbx, ty = dy - dn * h.nby, tz = dz - dn * h.nbz;
        double tang = Math.sqrt(tx * tx + ty * ty + tz * tz);
        return tang >= HAND_LOESEN || dn >= HAND_LOESEN;
    }

    /**
     * Bewegt beide Hände in Teilschritten (höchstens 1 cm) zu ihren Zielen. Neue Kontakte lassen die Hand
     * kleben und stoppen beide Hände. Gleitende Hände werden aus Flächen herausgedrückt; sie kleben wieder,
     * sobald sie nicht mehr weiterkommen, und werden frei, wenn der Kontakt (über {@code HALTE_RADIUS}) endet.
     *
     * @param dt     Zeitschritt in Sekunden
     * @param k      Körper
     * @param welt   Welt für Tastproben und Herausdrücken
     * @param zl     Ziel der linken Hand (Körperkoordinaten)
     * @param zr     Ziel der rechten Hand (Körperkoordinaten)
     * @param radius Suchradius des aktuellen Modus
     */
    private void bewege(double dt, Koerper k, Welt welt, double[] zl, double[] zr, double radius) {
        double maxS = HAND_GESCHW * dt;
        double[] dl = { zl[0] - links.bx, zl[1] - links.by, zl[2] - links.bz };
        double[] dr = { zr[0] - rechts.bx, zr[1] - rechts.by, zr[2] - rechts.bz };
        double ll = Math.sqrt(dl[0] * dl[0] + dl[1] * dl[1] + dl[2] * dl[2]);
        double lr = Math.sqrt(dr[0] * dr[0] + dr[1] * dr[1] + dr[2] * dr[2]);
        double sl = Math.min(ll, maxS), sr = Math.min(lr, maxS);
        double[] vorL = { links.bx, links.by, links.bz };
        double[] vorR = { rechts.bx, rechts.by, rechts.bz };

        int n = (int) Math.ceil(Math.max(sl, sr) / 0.01);
        for (int i = 0; i < n; i++) {
            schritt(links, dl, ll, sl / n);
            schritt(rechts, dr, lr, sr / n);
            for (Hand h : alle()) {
                if (h.gleitet) {
                    double[] w = weltPos(k, h);
                    welt.drueckeRaus(w, HAND_BLOCK_RADIUS);
                    ausWelt(k, h, w);
                }
            }
            boolean stopp = false;
            for (Hand h : alle()) {
                if (!h.gleitet) {
                    double[] w = weltPos(k, h);
                    Beruehrung b = welt.sondiere(w[0], w[1], w[2], radius, false);
                    if (b != null) {
                        festhalten(k, h, b);
                        h.neuerKontakt = true;
                        stopp = true;
                    }
                }
            }
            if (stopp) {
                break;
            }
        }

        // Gleitende Hände auswerten: Kontakt verloren -> frei; angekommen -> kleben
        for (Hand h : alle()) {
            if (!h.gleitet) {
                continue;
            }
            double[] vor = h.istLinks ? vorL : vorR;
            double moved = Math.sqrt((h.bx - vor[0]) * (h.bx - vor[0]) + (h.by - vor[1]) * (h.by - vor[1])
                    + (h.bz - vor[2]) * (h.bz - vor[2]));
            double[] w = weltPos(k, h);
            // Ein bestehender Kontakt bleibt beim Gleiten bis HALTE_RADIUS erhalten (Hysterese).
            Beruehrung b = welt.sondiere(w[0], w[1], w[2], HALTE_RADIUS, false);
            if (b == null) {
                h.gleitet = false;
            } else if (moved < 0.0005) {
                festhalten(k, h, b);
            }
        }
    }

    /**
     * Verschiebt eine Hand um eine Teilstrecke in Richtung ihres Ziels.
     *
     * @param h   Hand
     * @param d   Vektor von der Hand zum Ziel zu Beginn des Bildes
     * @param len Länge dieses Vektors
     * @param s   zurückzulegende Teilstrecke
     */
    private static void schritt(Hand h, double[] d, double len, double s) {
        if (len > 1e-9 && s > 0) {
            h.bx += d[0] / len * s;
            h.by += d[1] / len * s;
            h.bz += d[2] / len * s;
        }
    }
}
