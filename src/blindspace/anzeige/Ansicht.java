package blindspace.anzeige;

import static blindspace.konfig.Konstanten.*;

import blindspace.spiel.Spiel;
import blindspace.spieler.Eingabe;
import blindspace.spieler.Haende;
import blindspace.spieler.Koerper;
import blindspace.tasten.Tastkante;
import blindspace.tasten.Tastpunkt;
import blindspace.tasten.Verblassungsgruppe;
import blindspace.welt.Quader;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Darstellung und Eingabe: schwarzer Bildschirm, nur Tastpunkte, Hände und HUD.
 *
 * <p>
 * Die Weltgeometrie wird nie gerendert; nur die optionale Debug-Karte (F3) zeigt sie als Draufsicht. Die
 * Klasse übersetzt Tastatur- und Mausereignisse in den {@link Eingabe}-Zustand des Spiels und zeichnet die
 * Szene aus Sicht einer Kamera in Kopfmitte, die fest an die Körperausrichtung gebunden ist.
 * </p>
 */
@SuppressWarnings("serial")
public final class Ansicht extends JPanel {
    /** Versionskennung für die Serialisierung (nicht verwendet). */
    private static final long serialVersionUID = 1L;

    /** Randfarbe der Hände ohne Kontakt. */
    private static final Color HAND_RAND = new Color(235, 240, 255);

    /** Randfarbe der Hände bei Kontakt. */
    private static final Color HAND_RAND_KONTAKT = new Color(255, 200, 120);

    /** Textfarbe des HUD. */
    private static final Color HUD_TEXT = new Color(150, 170, 190);

    /** Größte Breite der Debug-Karte in Pixeln. */
    private static final double KARTE_MAX_BREITE = 224;

    /** Größte Höhe der Debug-Karte in Pixeln. */
    private static final double KARTE_MAX_HOEHE = 188;

    /** Das darzustellende Spiel. */
    private final Spiel spiel;

    /** Debug-Karte eingeblendet? */
    private boolean debugKarte;

    /** Hilfetext eingeblendet? */
    private boolean hilfe = true;

    /**
     * Erzeugt die Ansicht für ein Spiel, blendet den Mauszeiger aus und installiert die Eingabe-Listener.
     *
     * @param spiel das darzustellende Spiel
     */
    public Ansicht(Spiel spiel) {
        this.spiel = spiel;
        setBackground(Color.BLACK);
        setFocusable(true);
        if (!GraphicsEnvironment.isHeadless()) {
            // Der Mauszeiger ist an die Hände gekoppelt und wird ausgeblendet.
            setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new java.awt.Point(0, 0), "leer"));
        }
        installiereEingabe();
    }

    // ===== Eingabe =====

    /**
     * Installiert Tastatur-, Maus- und Fokus-Listener. Sie schreiben in {@link Spiel#eingabe}. Leertaste und
     * Num 0 lösen die Ausrichtung aus; F1 (Hilfe), F3 (Debug-Karte) und R (Neustart) steuern die Anzeige.
     */
    private void installiereEingabe() {
        final Eingabe in = spiel.eingabe;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                setze(e.getKeyCode(), true);
                switch (e.getKeyCode()) {
                case KeyEvent.VK_1 -> in.stufeWunsch = 0;
                case KeyEvent.VK_2 -> in.stufeWunsch = 1;
                case KeyEvent.VK_3 -> in.stufeWunsch = 2;
                case KeyEvent.VK_4 -> in.stufeWunsch = 3;
                case KeyEvent.VK_SPACE, KeyEvent.VK_NUMPAD0 -> in.ausrichten = true;
                case KeyEvent.VK_F3 -> debugKarte = !debugKarte;
                case KeyEvent.VK_F1 -> hilfe = !hilfe;
                case KeyEvent.VK_R -> spiel.reset();
                default -> {
                }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                setze(e.getKeyCode(), false);
            }

            /**
             * Setzt den Zustand der Geh- und Drehtasten.
             *
             * @param code Tastencode
             * @param wert true gedrückt, false losgelassen
             */
            private void setze(int code, boolean wert) {
                switch (code) {
                case KeyEvent.VK_W, KeyEvent.VK_UP -> in.vor = wert;
                case KeyEvent.VK_S, KeyEvent.VK_DOWN -> in.rueck = wert;
                case KeyEvent.VK_A, KeyEvent.VK_LEFT -> in.links = wert;
                case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> in.rechts = wert;
                default -> {
                }
                }
            }
        });
        MouseAdapter maus = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                bewegt(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                bewegt(e);
            }

            /**
             * Rechnet die Mausposition in den Mausbereich (zentral, {@code MAUS_BEREICH}) um.
             *
             * @param e Mausereignis
             */
            private void bewegt(MouseEvent e) {
                double rand = (1 - MAUS_BEREICH) / 2;
                double u = (e.getX() / (double) Math.max(1, getWidth()) - rand) / MAUS_BEREICH;
                double v = 1 - (e.getY() / (double) Math.max(1, getHeight()) - rand) / MAUS_BEREICH;
                in.mausU = Math.max(0, Math.min(1, u));
                in.mausV = Math.max(0, Math.min(1, v));
                in.mausBewegt = true;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    in.mausLinks = true;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    in.mausRechts = true;
                }
                in.mausBewegt = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    in.mausLinks = false;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    in.mausRechts = false;
                }
            }
        };
        addMouseListener(maus);
        addMouseMotionListener(maus);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                in.vor = in.rueck = in.links = in.rechts = false;
                in.mausLinks = in.mausRechts = false;
            }
        });
    }

    // ===== Zeichnen =====

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        zeichne((Graphics2D) g, getWidth(), getHeight());
    }

    /**
     * Blendet die Debug-Draufsicht ein oder aus (auch per F3 umschaltbar).
     *
     * @param an true zum Einblenden
     */
    public void setzeDebugKarte(boolean an) {
        debugKarte = an;
    }

    /**
     * Blendet den Hilfetext ein oder aus (auch per F1 umschaltbar).
     *
     * @param an true zum Einblenden
     */
    public void setzeHilfe(boolean an) {
        hilfe = an;
    }

    /**
     * Zeichnet ein komplettes Bild: Tastpunkte, Kanten, Hände, HUD und optional Debug-Karte und Hilfe. Auch ohne
     * Fenster nutzbar (z. B. für Bildtests).
     *
     * @param g Grafikkontext
     * @param w Breite in Pixeln
     * @param h Höhe in Pixeln
     */
    public void zeichne(Graphics2D g, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        Kamera cam = new Kamera(spiel.koerper, w, h);
        zeichnePunkte(g, cam);
        zeichneKanten(g, cam);
        zeichneHaende(g, cam);
        zeichneHud(g, w, h);
        if (debugKarte) {
            zeichneKarte(g, w);
        }
        if (hilfe) {
            zeichneHilfe(g, h);
        }
    }

    /** Perspektivische Kamera in Kopfmitte, fest an die Körperausrichtung gebunden. */
    private static final class Kamera {
        /** X-Position der Kamera in der Welt. */
        final double ex;
        /** Y-Position der Kamera in der Welt. */
        final double ey;
        /** Z-Position (Augenhöhe) der Kamera. */
        final double ez;
        /** Kosinus der Blickrichtung. */
        final double c;
        /** Sinus der Blickrichtung. */
        final double s;
        /** Brennweite in Pixeln (aus Sichtfeld und Bildbreite). */
        final double f;
        /** Bildmitte horizontal. */
        final double cx;
        /** Bildmitte vertikal. */
        final double cy;

        /**
         * Erzeugt die Kamera aus dem Körperzustand.
         *
         * @param k Körper (Position, Augenhöhe, Blickrichtung)
         * @param w Bildbreite in Pixeln
         * @param h Bildhöhe in Pixeln
         */
        Kamera(Koerper k, int w, int h) {
            ex = k.x;
            ey = k.y;
            ez = k.augenZ();
            c = Math.cos(k.kurs);
            s = Math.sin(k.kurs);
            f = (w / 2.0) / Math.tan(SICHTFELD / 2);
            cx = w / 2.0;
            cy = h / 2.0;
        }

        /**
         * Projiziert einen Weltpunkt perspektivisch auf den Bildschirm.
         *
         * @param wx X-Koordinate in der Welt
         * @param wy Y-Koordinate in der Welt
         * @param wz Z-Koordinate in der Welt
         * @return {Bildschirm-X, Bildschirm-Y, Tiefe} oder {@code null}, wenn der Punkt hinter der Kamera liegt
         */
        double[] projiziere(double wx, double wy, double wz) {
            double dx = wx - ex, dy = wy - ey, dz = wz - ez;
            double xv = dx * c + dy * s;
            double yv = -dx * s + dy * c;
            if (yv < 0.05) {
                return null;
            }
            return new double[] { cx + f * xv / yv, cy - f * dz / yv, yv };
        }

        /**
         * Projiziert eine Strecke und kürzt sie an der Bildebene, wenn ein Ende hinter der Kamera liegt.
         *
         * @param ax X-Koordinate des Anfangs in der Welt
         * @param ay Y-Koordinate des Anfangs in der Welt
         * @param az Z-Koordinate des Anfangs in der Welt
         * @param bx X-Koordinate des Endes in der Welt
         * @param by Y-Koordinate des Endes in der Welt
         * @param bz Z-Koordinate des Endes in der Welt
         * @return {Anfang-X, Anfang-Y, Anfang-Tiefe, Ende-X, Ende-Y, Ende-Tiefe} auf dem Bildschirm oder
         *         {@code null}, wenn die ganze Strecke hinter der Kamera liegt
         */
        double[] strecke(double ax, double ay, double az, double bx, double by, double bz) {
            double[] p = kameraRaum(ax, ay, az);
            double[] q = kameraRaum(bx, by, bz);
            final double nah = 0.05;
            if (p[1] < nah && q[1] < nah) {
                return null;
            }
            if (p[1] < nah) {
                p = schneide(p, q, nah);
            } else if (q[1] < nah) {
                q = schneide(q, p, nah);
            }
            return new double[] { cx + f * p[0] / p[1], cy - f * p[2] / p[1], p[1], cx + f * q[0] / q[1],
                    cy - f * q[2] / q[1], q[1] };
        }

        /**
         * Rechnet einen Weltpunkt in den Kameraraum um.
         *
         * @param wx X-Koordinate in der Welt
         * @param wy Y-Koordinate in der Welt
         * @param wz Z-Koordinate in der Welt
         * @return {rechts, Tiefe, hoch} relativ zur Kamera in Metern
         */
        private double[] kameraRaum(double wx, double wy, double wz) {
            double dx = wx - ex, dy = wy - ey, dz = wz - ez;
            return new double[] { dx * c + dy * s, -dx * s + dy * c, dz };
        }

        /**
         * Verschiebt einen Punkt hinter der Bildebene entlang der Strecke auf die Bildebene.
         *
         * @param hinten Punkt mit Tiefe unter {@code tiefe} (Kameraraum)
         * @param vorn   Punkt mit Tiefe ab {@code tiefe} (Kameraraum)
         * @param tiefe  Tiefe der Bildebene
         * @return der Schnittpunkt der Strecke mit der Bildebene (Kameraraum)
         */
        private static double[] schneide(double[] hinten, double[] vorn, double tiefe) {
            double t = (tiefe - hinten[1]) / (vorn[1] - hinten[1]);
            return new double[] { hinten[0] + t * (vorn[0] - hinten[0]), tiefe,
                    hinten[2] + t * (vorn[2] - hinten[2]) };
        }
    }

    /**
     * Zeichnet alle ertasteten Kantenstücke als scharfe weiße Linien mit weichem Schein und dunklem Saum, über den
     * Tastpunkten, damit keine Kugel eine Kante verdeckt und die Linie auch auf hellem Grund sichtbar bleibt. Die Deckkraft ist die Helligkeit des Stücks; die Linienbreite wächst perspektivisch, bleibt aber mindestens
     * {@code KANTE_MIN_PIXEL} breit, damit auch entfernte Kanten gut sichtbar sind.
     *
     * @param g   Grafikkontext
     * @param cam Kamera
     */
    private void zeichneKanten(Graphics2D g, Kamera cam) {
        double zeit = spiel.zeit();
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        for (Tastkante k : spiel.tastpunkte.kanten()) {
            double hell = Math.min(1.0, k.helligkeit(zeit));
            if (hell < 0.004) {
                continue;
            }
            double[] l = cam.strecke(k.stueck.x0(), k.stueck.y0(), k.stueck.z0(), k.stueck.x1(), k.stueck.y1(),
                    k.stueck.z1());
            if (l == null) {
                continue;
            }
            double tiefe = (l[2] + l[5]) / 2;
            float breite = (float) Math.max(KANTE_MIN_PIXEL, cam.f * KANTE_BREITE / tiefe);
            Line2D.Double linie = new Line2D.Double(l[0], l[1], l[3], l[4]);
            // Schein (weiß, breit, schwach), dunkler Saum (hebt die Linie von hellen Tastpunkten ab), heller Kern
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.28 * hell)));
            g.setStroke(new BasicStroke(breite * 3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            g.draw(linie);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.85 * hell)));
            g.setStroke(new BasicStroke(breite * 2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.BLACK);
            g.draw(linie);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) hell));
            g.setStroke(new BasicStroke(breite, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            g.draw(linie);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setStroke(new BasicStroke(1f));
    }

    /**
     * Zeichnet alle Kurzzeitpunkte und die Verblassungsgruppen des Langzeitgedächtnisses als verblassende Kugeln (radialer Farbverlauf, Deckkraft = Helligkeit).
     *
     * @param g   Grafikkontext
     * @param cam Kamera
     */
    private void zeichnePunkte(Graphics2D g, Kamera cam) {
        double zeit = spiel.zeit();
        int breite = getWidth() > 0 ? getWidth() : 1280;
        int hoehe = getHeight() > 0 ? getHeight() : 720;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (Tastpunkt p : spiel.tastpunkte.alle()) {
            double hell = Math.min(1.0, p.helligkeit(zeit));
            if (hell < 0.004) {
                continue;
            }
            double[] s = cam.projiziere(p.x, p.y, p.z);
            if (s == null) {
                continue;
            }
            float r = (float) Math.max(2.0, cam.f * (p.durchmesser / 2) / s[2]);
            if (s[0] + r < 0 || s[0] - r > breite || s[1] + r < 0 || s[1] - r > hoehe) {
                continue;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) hell));
            g.drawImage(Muster.sprite(p.oberflaeche), (int) Math.round(s[0] - r), (int) Math.round(s[1] - r),
                    Math.round(2 * r), Math.round(2 * r), null);
        }
        for (Verblassungsgruppe gr : spiel.tastpunkte.gruppen()) {
            double hell = Math.min(1.0, gr.helligkeit(zeit));
            if (hell < 0.002) {
                continue;
            }
            double[] s = cam.projiziere(gr.x(), gr.y(), gr.z());
            if (s == null) {
                continue;
            }
            float r = (float) Math.max(2.0, cam.f * gr.radius(zeit) / s[2]);
            if (s[0] + r < 0 || s[0] - r > breite || s[1] + r < 0 || s[1] - r > hoehe) {
                continue;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) hell));
            g.drawImage(Muster.sprite(gr.oberflaeche()), (int) Math.round(s[0] - r), (int) Math.round(s[1] - r),
                    Math.round(2 * r), Math.round(2 * r), null);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    /**
     * Zeichnet die Hände als transparente Ellipsoide, entlang der Armrichtung (Schulter zur Hand) orientiert.
     * Bei Kontakt wird der Rand orange.
     *
     * @param g   Grafikkontext
     * @param cam Kamera
     */
    private void zeichneHaende(Graphics2D g, Kamera cam) {
        Koerper k = spiel.koerper;
        double schulterZ = k.beinHoehe() + SCHULTER_OFFSET;
        double c = Math.cos(k.kurs), s = Math.sin(k.kurs);
        for (Haende.Hand h : spiel.haende.alle()) {
            double[] w = Haende.weltPos(k, h);
            // Armrichtung: von der Schulter zur Hand
            double sx = h.istLinks ? -SCHULTER_X : SCHULTER_X;
            double sy = SCHULTER_Y;
            double ax = h.bx - sx, ay = h.by - sy, az = h.bz - schulterZ;
            double len = Math.sqrt(ax * ax + ay * ay + az * az);
            if (len < 1e-6) {
                ax = 0;
                ay = 1;
                az = 0;
                len = 1;
            }
            ax /= len;
            ay /= len;
            az /= len;
            // Körperrichtung -> Welt
            double dwx = ax * c - ay * s, dwy = ax * s + ay * c, dwz = az;
            double hl = HAND_LAENGE / 2;
            double[] p0 = cam.projiziere(w[0] - dwx * hl, w[1] - dwy * hl, w[2] - dwz * hl);
            double[] p1 = cam.projiziere(w[0] + dwx * hl, w[1] + dwy * hl, w[2] + dwz * hl);
            double[] pm = cam.projiziere(w[0], w[1], w[2]);
            if (pm == null) {
                continue;
            }
            double breite = Math.max(4, cam.f * HAND_BREITE / pm[2]);
            double lx = 0, ly = 0;
            if (p0 != null && p1 != null) {
                lx = p1[0] - p0[0];
                ly = p1[1] - p0[1];
            }
            double laenge = Math.hypot(lx, ly);
            double winkel = laenge > 1e-3 ? Math.atan2(ly, lx) : 0;
            laenge = Math.max(laenge, breite * 1.3);

            AffineTransform alt = g.getTransform();
            g.translate(pm[0], pm[1]);
            g.rotate(winkel);
            Ellipse2D.Double e = new Ellipse2D.Double(-laenge / 2, -breite / 2, laenge, breite);
            boolean kontakt = h.kontakt != null;
            g.setColor(new Color(220, 230, 255, kontakt ? 95 : 55));
            g.fill(e);
            g.setStroke(new BasicStroke(1.8f));
            g.setColor(kontakt ? HAND_RAND_KONTAKT : HAND_RAND);
            g.draw(e);
            g.setTransform(alt);
        }
    }

    /**
     * Zeichnet die HUD-Kopfleiste: Modus, Körperhöhe, Tastpunkte, Kollisionen, Handkontakt links/rechts,
     * Ausrichtungen, Wandführung und ggf. den Hinweis, dass eine Ausrichtung möglich ist.
     *
     * @param g Grafikkontext
     * @param w Bildbreite
     * @param h Bildhöhe
     */
    private void zeichneHud(Graphics2D g, int w, int h) {
        Koerper k = spiel.koerper;
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, w, 26);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g.setColor(HUD_TEXT);
        Haende hd = spiel.haende;
        String text = String.format("Modus: %s | Höhe: %d cm | Tastpunkte: %d (+%d Gruppen) | Kanten: %d | Kollisionen: %d | Kontakt L[%s] R[%s]"
                + " | Ausrichtungen: %d | Wandführung: %s%s", k.gehen() ? "Gehen" : "Stehen",
                Math.round(k.beinHoehe() * 100), spiel.tastpunkte.anzahl(), spiel.tastpunkte.anzahlGruppen(), spiel.tastpunkte.anzahlKanten(),
                k.kollisionen(),
                hd.links.kontakt != null ? "x" : " ", hd.rechts.kontakt != null ? "x" : " ",
                spiel.ausrichtung.anzahl(), spiel.ausrichtung.wandfuehrung() ? "an" : "aus",
                spiel.ausrichtung.moeglich() ? " | Ausrichtung möglich" : "");
        g.drawString(text, 8, 18);
    }

    /**
     * Zeichnet den Hilfetext am unteren Bildrand.
     *
     * @param g Grafikkontext
     * @param h Bildhöhe
     */
    private void zeichneHilfe(Graphics2D g, int h) {
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.setColor(new Color(110, 125, 140));
        String[] zeilen = { "W/S oder Pfeile hoch/runter: gehen (geduckt: Schritte à 10 cm)   A/D oder Pfeile links/rechts: drehen",
                "1-4: Körperhöhe (aufrecht ... geduckt)   Maus: Hände führen (Höhe, Spannweite)   Leertaste/Num 0: ausrichten",
                "linke/rechte Maustaste: nach links/rechts tasten   F1: Hilfe   F3: Debug-Karte   R: Neustart" };
        for (int i = 0; i < zeilen.length; i++) {
            g.drawString(zeilen[i], 8, h - 8 - (zeilen.length - 1 - i) * 16);
        }
    }

    /**
     * Zeichnet die Debug-Draufsicht (F3) oben rechts: Quader, Körper mit Blickrichtung und Hände. Der
     * Maßstab ergibt sich aus der Ausdehnung der Welt, funktioniert also für jedes Level. Nur zum Testen; die
     * Karte widerspricht bewusst dem Blind-Konzept.
     *
     * @param g Grafikkontext
     * @param w Bildbreite
     */
    private void zeichneKarte(Graphics2D g, int w) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Quader q : spiel.welt.quader()) {
            minX = Math.min(minX, q.minX);
            minY = Math.min(minY, q.minY);
            maxX = Math.max(maxX, q.maxX);
            maxY = Math.max(maxY, q.maxY);
        }
        if (minX > maxX) {
            return; // leere Welt
        }
        double breite = Math.max(1e-6, maxX - minX), hoehe = Math.max(1e-6, maxY - minY);
        double m = Math.min(KARTE_MAX_BREITE / breite, KARTE_MAX_HOEHE / hoehe); // Pixel pro Meter
        double mx = w - breite * m - 12, my = 36;
        g.setColor(new Color(0, 0, 0, 150));
        g.fill(new Rectangle2D.Double(mx - 6, my - 6, breite * m + 12, hoehe * m + 12));
        g.setColor(new Color(90, 100, 110));
        for (Quader q : spiel.welt.quader()) {
            g.fill(new Rectangle2D.Double(mx + (q.minX - minX) * m, my + (maxY - q.maxY) * m,
                    (q.maxX - q.minX) * m, (q.maxY - q.minY) * m));
        }
        Koerper k = spiel.koerper;
        double bx = mx + (k.x - minX) * m, by = my + (maxY - k.y) * m;
        g.setColor(new Color(255, 200, 120));
        g.fill(new Ellipse2D.Double(bx - 4, by - 4, 8, 8));
        g.setStroke(new BasicStroke(2f));
        g.drawLine((int) bx, (int) by, (int) (bx - Math.sin(k.kurs) * 16), (int) (by - Math.cos(k.kurs) * 16));
        g.setColor(new Color(190, 225, 255));
        for (Haende.Hand hd : spiel.haende.alle()) {
            double[] wp = Haende.weltPos(k, hd);
            g.fill(new Ellipse2D.Double(mx + (wp[0] - minX) * m - 2.5, my + (maxY - wp[1]) * m - 2.5, 5, 5));
        }
    }
}
