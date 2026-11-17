package blindspace.welt;

import blindspace.konfig.Konstanten;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Die unsichtbare Spielwelt: achsenparallele Quader auf einem virtuellen Boden (z = 0, ID {@link #BODEN_ID}).
 *
 * <p>
 * Die Klasse bietet Tastproben für die Hände ({@link #sondiere}), das Herausdrücken einer gleitenden Hand
 * aus Objekten ({@link #drueckeRaus}), die dreidimensionale Körperkollision ({@link #koerperKollidiert})
 * und eine Abstandsabfrage für die Wandführung ({@link #naechsterAbstand}). Konkrete Welten werden in
 * Klassen des Pakets {@code blindspace.levels} aufgebaut.
 * </p>
 */
public final class Welt {
    /**
     * Eine Körperkollision: getroffenes Körperteil, getroffenes Objekt und Kontaktpunkt.
     *
     * @param teil     zuerst getroffenes Körperteil (Beine haben Vorrang vor Rumpf und Kopf)
     * @param objektId ID des getroffenen Quaders
     * @param oberflaeche Oberflächenart des getroffenen Quaders
     * @param px       X-Koordinate des Kontaktpunkts
     * @param py       Y-Koordinate des Kontaktpunkts
     * @param pz       Z-Koordinate des Kontaktpunkts
     */
    public record Kollision(Koerperteil teil, int objektId, Oberflaeche oberflaeche, double px, double py, double pz) {
    }

    /** Reservierte ID für den virtuellen Boden. */
    public static final int BODEN_ID = -1;

    /** Toleranz für Gleitkommavergleiche. */
    private static final double EPS = 1e-9;

    /** Alle Quader der Welt; die ID ergibt sich aus der Reihenfolge des Hinzufügens (ab 1). */
    private final List<Quader> quader = new ArrayList<>();

    /** Erzeugt eine leere Welt ohne Quader (nur der virtuelle Boden existiert). */
    public Welt() {
    }

    /**
     * Liefert alle Quader der Welt.
     *
     * @return unveränderliche Sicht auf die Quaderliste
     */
    public List<Quader> quader() {
        return Collections.unmodifiableList(quader);
    }

    /**
     * Fügt einen Quader hinzu, der auf dem Boden steht (Unterkante z = 0). Die ID wird fortlaufend vergeben.
     *
     * @param name        Anzeigename
     * @param art         Objektkategorie
     * @param oberflaeche Oberflächenart
     * @param x0          kleinste X-Koordinate
     * @param y0          kleinste Y-Koordinate
     * @param x1          größte X-Koordinate
     * @param y1          größte Y-Koordinate
     * @param hoehe       Höhe des Quaders
     */
    public void add(String name, Objektart art, Oberflaeche oberflaeche, double x0, double y0, double x1, double y1,
            double hoehe) {
        quader.add(new Quader(quader.size() + 1, name, art, oberflaeche, x0, y0, 0, x1, y1, hoehe));
    }

    /**
     * Kurzform von {@link #add(String, Objektart, Oberflaeche, double, double, double, double, double)} für
     * einfache Hindernisse (Kategorie {@code SONSTIGES}, glatte Oberfläche), z. B. in Tests.
     *
     * @param name  Anzeigename
     * @param x0    kleinste X-Koordinate
     * @param y0    kleinste Y-Koordinate
     * @param x1    größte X-Koordinate
     * @param y1    größte Y-Koordinate
     * @param hoehe Höhe des Quaders
     */
    public void add(String name, double x0, double y0, double x1, double y1, double hoehe) {
        add(name, Objektart.SONSTIGES, Oberflaeche.GLATT, x0, y0, x1, y1, hoehe);
    }

    /**
     * Sucht einen Quader anhand seiner ID. Grundlage für spätere Interaktionen (Objekt zu einer Berührung).
     *
     * @param id ID des Quaders (aus {@link Beruehrung#objektId()})
     * @return der Quader oder {@code null}, wenn die ID unbekannt ist (z. B. der Boden)
     */
    public Quader objekt(int id) {
        return id >= 1 && id <= quader.size() ? quader.get(id - 1) : null;
    }

    /**
     * Fügt einen Quader hinzu, der frei über dem Boden schwebt (z. B. Hängeobjekt, Balken, Unterseite einer
     * Treppe oder Decke). Die ID wird fortlaufend vergeben.
     *
     * @param name Anzeigename
     * @param x0   kleinste X-Koordinate
     * @param y0   kleinste Y-Koordinate
     * @param z0   Unterkante
     * @param x1   größte X-Koordinate
     * @param y1   größte Y-Koordinate
     * @param z1   Oberkante
     */
    public void addSchwebend(String name, double x0, double y0, double z0, double x1, double y1, double z1) {
        quader.add(new Quader(quader.size() + 1, name, Objektart.SONSTIGES, Oberflaeche.GLATT, x0, y0, z0, x1, y1,
                z1));
    }

    /**
     * Begrenzt einen Wert auf ein Intervall.
     *
     * @param v  Wert
     * @param lo untere Grenze
     * @param hi obere Grenze
     * @return {@code v}, begrenzt auf [lo, hi]
     */
    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    /**
     * Tastprobe: sucht das nächste Objekt (oder den Boden) innerhalb einer Kugel um einen Punkt.
     *
     * <p>
     * Liegt der Punkt außerhalb eines Quaders, ergibt sich die getroffene Fläche aus der Achse mit dem größten
     * Abstand. Liegt er im Quader, wird die nächste Außenfläche gewählt.
     * </p>
     *
     * @param x      X-Koordinate des Probenmittelpunkts
     * @param y      Y-Koordinate des Probenmittelpunkts
     * @param z      Z-Koordinate des Probenmittelpunkts
     * @param radius Radius der Probenkugel
     * @return die nächste {@link Beruehrung} oder {@code null}, wenn nichts im Radius liegt
     */
    public Beruehrung sondiere(double x, double y, double z, double radius) {
        Beruehrung best = null;
        for (Quader q : quader) {
            double cx = clamp(x, q.minX, q.maxX);
            double cy = clamp(y, q.minY, q.maxY);
            double cz = clamp(z, q.minZ, q.maxZ);
            double dx = x - cx, dy = y - cy, dz = z - cz;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > radius * radius) {
                continue;
            }
            double d = Math.sqrt(d2);
            Beruehrung b;
            if (d > EPS) {
                double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
                if (ax >= ay && ax >= az) {
                    b = new Beruehrung(q.id, 0, dx > 0 ? 1 : -1, cx, cy, cz, d, q.oberflaeche, q.art);
                } else if (ay >= az) {
                    b = new Beruehrung(q.id, 1, dy > 0 ? 1 : -1, cx, cy, cz, d, q.oberflaeche, q.art);
                } else {
                    b = new Beruehrung(q.id, 2, dz > 0 ? 1 : -1, cx, cy, cz, d, q.oberflaeche, q.art);
                }
            } else {
                // Punkt liegt im Quader: nächste Fläche wählen
                double[] pen = { x - q.minX, q.maxX - x, y - q.minY, q.maxY - y, z - q.minZ, q.maxZ - z };
                int k = 0;
                for (int i = 1; i < 6; i++) {
                    if (pen[i] < pen[k]) {
                        k = i;
                    }
                }
                switch (k) {
                case 0 -> b = new Beruehrung(q.id, 0, -1, q.minX, y, z, 0, q.oberflaeche, q.art);
                case 1 -> b = new Beruehrung(q.id, 0, 1, q.maxX, y, z, 0, q.oberflaeche, q.art);
                case 2 -> b = new Beruehrung(q.id, 1, -1, x, q.minY, z, 0, q.oberflaeche, q.art);
                case 3 -> b = new Beruehrung(q.id, 1, 1, x, q.maxY, z, 0, q.oberflaeche, q.art);
                case 4 -> b = new Beruehrung(q.id, 2, -1, x, y, q.minZ, 0, q.oberflaeche, q.art);
                default -> b = new Beruehrung(q.id, 2, 1, x, y, q.maxZ, 0, q.oberflaeche, q.art);
                }
            }
            if (best == null || b.abstand() < best.abstand()) {
                best = b;
            }
        }
        if (z <= radius) {
            double d = Math.max(0, z);
            if (best == null || d < best.abstand()) {
                best = new Beruehrung(BODEN_ID, 2, 1, x, y, 0, d, Oberflaeche.RAU, Objektart.BODEN);
            }
        }
        return best;
    }

    /**
     * Drückt einen Punkt so aus allen Quadern (und dem Boden), dass er mindestens {@code radius} Abstand hat.
     * Wird für gleitende Hände verwendet, die an Flächen entlangfahren.
     *
     * @param p      Punkt {x, y, z}; wird direkt verändert
     * @param radius gewünschter Mindestabstand zu allen Oberflächen
     */
    public void drueckeRaus(double[] p, double radius) {
        for (int iter = 0; iter < 3; iter++) {
            boolean geaendert = false;
            for (Quader q : quader) {
                double cx = clamp(p[0], q.minX, q.maxX);
                double cy = clamp(p[1], q.minY, q.maxY);
                double cz = clamp(p[2], q.minZ, q.maxZ);
                double dx = p[0] - cx, dy = p[1] - cy, dz = p[2] - cz;
                double d2 = dx * dx + dy * dy + dz * dz;
                if (d2 >= radius * radius) {
                    continue;
                }
                double d = Math.sqrt(d2);
                if (d > EPS) {
                    double s = radius / d;
                    p[0] = cx + dx * s;
                    p[1] = cy + dy * s;
                    p[2] = cz + dz * s;
                } else {
                    double[] pen = { p[0] - q.minX, q.maxX - p[0], p[1] - q.minY, q.maxY - p[1], p[2] - q.minZ,
                            q.maxZ - p[2] };
                    int k = 0;
                    for (int i = 1; i < 6; i++) {
                        if (pen[i] < pen[k]) {
                            k = i;
                        }
                    }
                    switch (k) {
                    case 0 -> p[0] = q.minX - radius;
                    case 1 -> p[0] = q.maxX + radius;
                    case 2 -> p[1] = q.minY - radius;
                    case 3 -> p[1] = q.maxY + radius;
                    case 4 -> p[2] = q.minZ - radius;
                    default -> p[2] = q.maxZ + radius;
                    }
                }
                geaendert = true;
            }
            if (p[2] < radius) {
                p[2] = radius;
                geaendert = true;
            }
            if (!geaendert) {
                break;
            }
        }
    }

    // ===== Körperkollision (3D: Beine, Oberkörper, Kopf) =====

    /**
     * Überlappen sich der Quader und ein Höhenbereich (mit Toleranz)?
     *
     * @param q  Quader
     * @param z0 Unterkante des Bereichs
     * @param z1 Oberkante des Bereichs
     * @return true bei Überlappung
     */
    private static boolean zUeberlapp(Quader q, double z0, double z1) {
        return q.minZ < z1 - EPS && q.maxZ > z0 + EPS;
    }

    /**
     * Schneidet ein Kreis (Draufsicht) das Rechteck des Quaders?
     *
     * @param q Quader
     * @param x X-Mittelpunkt des Kreises
     * @param y Y-Mittelpunkt des Kreises
     * @param r Radius des Kreises
     * @return true bei Überschneidung
     */
    private static boolean kreisSchneidet(Quader q, double x, double y, double r) {
        double dx = x - clamp(x, q.minX, q.maxX);
        double dy = y - clamp(y, q.minY, q.maxY);
        return dx * dx + dy * dy < r * r - EPS;
    }

    /**
     * Schneidet ein gedrehtes Rechteck (Draufsicht) das Rechteck des Quaders? Verwendet den
     * Separating-Axis-Test mit den zwei Weltachsen und den zwei Körperachsen.
     *
     * @param q    Quader
     * @param x    X-Mittelpunkt des gedrehten Rechtecks
     * @param y    Y-Mittelpunkt des gedrehten Rechtecks
     * @param kurs Blickwinkel des Körpers (0 = +Y, gegen den Uhrzeigersinn)
     * @param a    halbe Breite (quer zur Blickrichtung)
     * @param b    halbe Tiefe (längs der Blickrichtung)
     * @return true bei Überschneidung
     */
    private static boolean rechteckSchneidet(Quader q, double x, double y, double kurs, double a, double b) {
        double ux = Math.cos(kurs), uy = Math.sin(kurs); // rechts
        double fx = -Math.sin(kurs), fy = Math.cos(kurs); // vorwärts
        double cx = (q.minX + q.maxX) / 2, cy = (q.minY + q.maxY) / 2;
        double hx = (q.maxX - q.minX) / 2, hy = (q.maxY - q.minY) / 2;
        double dx = cx - x, dy = cy - y;
        // Weltachsen
        if (Math.abs(dx) > hx + a * Math.abs(ux) + b * Math.abs(fx) - EPS) {
            return false;
        }
        if (Math.abs(dy) > hy + a * Math.abs(uy) + b * Math.abs(fy) - EPS) {
            return false;
        }
        // Körperachsen
        if (Math.abs(dx * ux + dy * uy) > a + hx * Math.abs(ux) + hy * Math.abs(uy) - EPS) {
            return false;
        }
        if (Math.abs(dx * fx + dy * fy) > b + hx * Math.abs(fx) + hy * Math.abs(fy) - EPS) {
            return false;
        }
        return true;
    }

    /**
     * Prüft alle drei Körperformen (gestapelt: Beinzylinder, Oberkörper-Quader, Kopfzylinder) in 3D gegen
     * alle Quader der Welt. Der Boden zählt nicht als Hindernis.
     *
     * @param x     X-Position der Körpermitte
     * @param y     Y-Position der Körpermitte
     * @param kurs  Blickwinkel des Körpers
     * @param stufe Körperstufe 0 (aufrecht) bis 3 (geduckt)
     * @return true, wenn der Körper mit irgendeinem Quader kollidiert
     */
    public boolean koerperKollidiert(double x, double y, double kurs, int stufe) {
        return koerperKollision(x, y, kurs, stufe) != null;
    }

    /**
     * Wie {@link #koerperKollidiert}, liefert aber Körperteil, Objekt und Kontaktpunkt. Berührt der Körper
     * mehrere Teile, haben die Beine Vorrang (der Fuß trifft zuerst), danach Rumpf und Kopf.
     *
     * @param x     X-Position der Körpermitte
     * @param y     Y-Position der Körpermitte
     * @param kurs  Blickwinkel des Körpers
     * @param stufe Körperstufe 0 (aufrecht) bis 3 (geduckt)
     * @return die Kollision oder {@code null}, wenn der Körper frei ist
     */
    public Kollision koerperKollision(double x, double y, double kurs, int stufe) {
        double h = Konstanten.BEIN_HOEHEN[stufe];
        double torsoOben = h + Konstanten.TORSO_HOEHE;
        double kopfOben = torsoOben + Konstanten.KOPF_HOEHE;
        Kollision best = null;
        for (Quader q : quader) {
            if (zUeberlapp(q, 0, h) && kreisSchneidet(q, x, y, Konstanten.BEIN_RADIUS)) {
                return kontakt(Koerperteil.BEIN, q, x, y, 0, h);
            }
            if (best != null) {
                continue;
            }
            if (zUeberlapp(q, h, torsoOben)
                    && rechteckSchneidet(q, x, y, kurs, Konstanten.TORSO_BREITE / 2, Konstanten.TORSO_TIEFE / 2)) {
                best = kontakt(Koerperteil.RUMPF, q, x, y, h, torsoOben);
            } else if (zUeberlapp(q, torsoOben, kopfOben) && kreisSchneidet(q, x, y, Konstanten.KOPF_RADIUS)) {
                best = kontakt(Koerperteil.KOPF, q, x, y, torsoOben, kopfOben);
            }
        }
        return best;
    }

    /**
     * Bestimmt den Kontaktpunkt zwischen einem Körperteil und einem Quader: in der Draufsicht der dem
     * Körpermittelpunkt nächste Punkt des Quaders, in der Höhe die Mitte des gemeinsamen Höhenbereichs.
     *
     * @param teil getroffenes Körperteil
     * @param q    getroffener Quader
     * @param x    X-Position der Körpermitte
     * @param y    Y-Position der Körpermitte
     * @param z0   Unterkante des Körperteils
     * @param z1   Oberkante des Körperteils
     * @return die Kollision mit Kontaktpunkt
     */
    private static Kollision kontakt(Koerperteil teil, Quader q, double x, double y, double z0, double z1) {
        double lo = Math.max(q.minZ, z0), hi = Math.min(q.maxZ, z1);
        return new Kollision(teil, q.id, q.oberflaeche, clamp(x, q.minX, q.maxX), clamp(y, q.minY, q.maxY), (lo + hi) / 2);
    }

    /**
     * Kleinster horizontaler Abstand eines Punkts zu einem Quader, dessen Höhe das Tastband überlappt.
     * Dient der Wandführung, um zu erkennen, ob noch ein Objekt in Handreichweite ist.
     *
     * @param x   X-Koordinate des Punkts
     * @param y   Y-Koordinate des Punkts
     * @param zLo untere Grenze des Tastbands
     * @param zHi obere Grenze des Tastbands
     * @return kleinster Abstand in Metern oder {@link Double#POSITIVE_INFINITY}, wenn kein Quader das Band berührt
     */
    public double naechsterAbstand(double x, double y, double zLo, double zHi) {
        double best = Double.POSITIVE_INFINITY;
        for (Quader q : quader) {
            if (!zUeberlapp(q, zLo, zHi)) {
                continue;
            }
            double dx = x - clamp(x, q.minX, q.maxX);
            double dy = y - clamp(y, q.minY, q.maxY);
            best = Math.min(best, Math.hypot(dx, dy));
        }
        return best;
    }
}
