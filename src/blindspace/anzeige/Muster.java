package blindspace.anzeige;

import blindspace.welt.Oberflaeche;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Vorgerenderte Bilder der Tastpunkt-Visualisierung je Oberflächenart (GDD Abschnitt 2 und 3).
 *
 * <p>
 * Die Darstellung ist ausschließlich weiß auf schwarz: Die Oberflächenart zeigt sich nur im Muster innerhalb der
 * runden, weich auslaufenden Kugel. Die Bilder werden einmal erzeugt und pro Punkt nur skaliert und mit der
 * Helligkeit als Deckkraft gezeichnet.
 * </p>
 * <ul>
 * <li>{@link Oberflaeche#GLATT}: gleichmäßig, glatter Verlauf.</li>
 * <li>{@link Oberflaeche#RAU}: grobes, unregelmäßiges Punktmuster.</li>
 * <li>{@link Oberflaeche#STRUKTURIERT}: waagerechte, leicht gewellte Streifen (Maserung).</li>
 * </ul>
 */
final class Muster {
    /** Kantenlänge der Bilder in Pixeln. */
    private static final int N = 128;

    /** Anteil des Radius mit voller Deckkraft im Kern. */
    private static final double KERN = 0.55;

    /** Die Bilder je Oberflächenart. */
    private static final Map<Oberflaeche, BufferedImage> SPRITES = new EnumMap<>(Oberflaeche.class);

    static {
        for (Oberflaeche o : Oberflaeche.values()) {
            SPRITES.put(o, erzeuge(o));
        }
    }

    /** Reine Hilfsklasse, keine Instanzen. */
    private Muster() {
    }

    /**
     * Liefert das Bild für eine Oberflächenart.
     *
     * @param o Oberflächenart
     * @return quadratisches weißes Bild mit Alphakanal (Kugel mit Muster)
     */
    static BufferedImage sprite(Oberflaeche o) {
        return SPRITES.get(o);
    }

    /**
     * Radialer Verlauf der Kugel: volle Deckkraft im Kern, linear auf etwa 55 % bei {@code KERN}, bis 0 am Rand.
     *
     * @param d normierter Abstand vom Mittelpunkt (0 = Mitte, 1 = Rand)
     * @return Deckkraft 0..1
     */
    private static double verlauf(double d) {
        if (d >= 1) {
            return 0;
        }
        if (d <= KERN) {
            return 1 - 0.45 * (d / KERN);
        }
        return 0.55 * (1 - (d - KERN) / (1 - KERN));
    }

    /**
     * Weicher Zufallswert (bilineare Wertestreuung) für das Rau-Muster: ergibt unregelmäßige, runde Flecken.
     *
     * @param punkte Zufallswerte an den Gitterpunkten ((zellen + 1) x (zellen + 1))
     * @param zellen Anzahl der Zellen je Richtung
     * @param x      Pixelspalte
     * @param y      Pixelzeile
     * @return Wert zwischen 0 und 1
     */
    private static double rauwert(double[] punkte, int zellen, int x, int y) {
        double fx = (double) x * zellen / N, fy = (double) y * zellen / N;
        int ix = (int) fx, iy = (int) fy;
        double ax = fx - ix, ay = fy - iy;
        ax = ax * ax * (3 - 2 * ax);
        ay = ay * ay * (3 - 2 * ay);
        int w = zellen + 1;
        double o = punkte[iy * w + ix] * (1 - ax) + punkte[iy * w + ix + 1] * ax;
        double u = punkte[(iy + 1) * w + ix] * (1 - ax) + punkte[(iy + 1) * w + ix + 1] * ax;
        return o * (1 - ay) + u * ay;
    }

    /**
     * Erzeugt das Bild einer Oberflächenart.
     *
     * @param o Oberflächenart
     * @return Bild mit weißen Pixeln und Alphakanal
     */
    private static BufferedImage erzeuge(Oberflaeche o) {
        BufferedImage img = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
        Random zufall = new Random(1234);
        int zellen = 10;
        double[] punkte = new double[(zellen + 1) * (zellen + 1)];
        for (int i = 0; i < punkte.length; i++) {
            punkte[i] = zufall.nextDouble();
        }
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                double dx = (x + 0.5 - N / 2.0) / (N / 2.0);
                double dy = (y + 0.5 - N / 2.0) / (N / 2.0);
                double deckkraft = verlauf(Math.sqrt(dx * dx + dy * dy));
                double muster = switch (o) {
                case GLATT -> 1.0;
                case RAU -> rauwert(punkte, zellen, x, y) < 0.5 ? 0.15 : 1.0;
                case STRUKTURIERT -> 0.55 + 0.45 * Math.sin(2 * Math.PI * (y + 4 * Math.sin(x / 18.0)) / 14.0);
                };
                int a = (int) Math.round(255 * Math.max(0, Math.min(1, deckkraft * muster)));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }
}
