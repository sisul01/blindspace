package blindspace.test;

import static blindspace.konfig.Konstanten.*;

import blindspace.anzeige.Ansicht;
import blindspace.spiel.Spiel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Rendert Beispielbilder (PNG) nach einem Skript, ohne Fenster. Aufruf mit
 * Zielordner als Argument (Standard: Arbeitsverzeichnis).
 */
public final class Bildtest {
    /** Testklasse, keine Instanzen. */
    private Bildtest() {
    }

    /**
     * Rendert fünf Beispielbilder (Start, Wand, seitliches Tasten, Kiste geduckt, Kanten am Pfeiler) als PNG.
     *
     * @param args optional der Zielordner (Standard: Arbeitsverzeichnis)
     * @throws Exception bei Schreibfehlern
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File dir = new File(args.length > 0 ? args[0] : ".");
        dir.mkdirs();

        // 1) Start: Hände in Startpose, noch nichts ertastet
        Spiel s = new Spiel(1);
        speichern(s, new File(dir, "1_start.png"), false);

        // 2) Zur Trennwand gehen und mit den Händen abtasten
        s.eingabe.mausBewegt = true;
        s.eingabe.vor = true;
        Selbsttest.lauf(s, 8.0);
        s.eingabe.vor = false;
        s.eingabe.mausU = 0.9;
        Selbsttest.lauf(s, 1.5);
        s.eingabe.mausU = 0.2;
        s.eingabe.mausV = 0.8;
        Selbsttest.lauf(s, 2.5);
        speichern(s, new File(dir, "2_wand.png"), true);

        // 3) Seitlich entlang der Ostwand tasten
        Spiel t = new Spiel(6);
        t.koerper.x = 5.3;
        t.koerper.y = -4.5;
        t.koerper.kurs = -0.1;
        t.eingabe.mausBewegt = true;
        t.eingabe.mausRechts = true;
        t.eingabe.mausV = 0.5;
        t.eingabe.vor = true;
        Selbsttest.lauf(t, 1.0);
        t.eingabe.vor = false;
        Selbsttest.lauf(t, 0.5);
        t.eingabe.ausrichten = true; // manuelle Ausrichtung (Leertaste)
        Selbsttest.lauf(t, 0.6);
        t.eingabe.vor = true;
        Selbsttest.lauf(t, 2.0);
        speichern(t, new File(dir, "3_seitlich.png"), true);

        // 4) Kiste: Kollision, geduckt abtasten
        Spiel u = new Spiel(3);
        u.koerper.x = 3.25;
        u.koerper.y = -2.0;
        u.koerper.kurs = Math.PI;
        u.eingabe.mausBewegt = true;
        u.eingabe.mausU = 0.2;
        u.eingabe.mausV = 0.5;
        u.eingabe.vor = true;
        for (int i = 0; i < 700 && u.koerper.kollisionen() == 0; i++) {
            u.schritt(SIM_DT);
        }
        u.eingabe.vor = false;
        Selbsttest.lauf(u, 1.0);
        u.eingabe.stufeWunsch = 3;
        Selbsttest.lauf(u, 3.0);
        speichern(u, new File(dir, "4_kiste_geduckt.png"), true);

        // 5) Pfeiler: Die Hände treten in mehreren Höhen an die senkrechten Vorderkanten heran (stehend und
        // geduckt); die Kantenstücke wachsen zu einer durchgehenden Linie zusammen (Konzept 8.5)
        Spiel v = new Spiel(5);
        v.koerper.x = 1.2;
        v.koerper.kurs = 0;
        v.eingabe.mausBewegt = true;
        v.eingabe.mausU = 0.25;
        for (int stufe = 0; stufe <= 1; stufe++) {
            v.eingabe.stufeWunsch = stufe;
            for (double hoehe : new double[] { 0.95, 0.5, 0.05 }) {
                v.eingabe.mausV = hoehe;
                Selbsttest.geheY(v, 0.9); // zurücktreten: die Hände lösen sich von der Kante
                Selbsttest.lauf(v, 0.5);
                Selbsttest.geheY(v, 1.3); // wieder herantreten: die Hände treffen die Kante
                Selbsttest.lauf(v, 0.3);
            }
        }
        speichern(v, new File(dir, "5_kanten_pfeiler.png"), true);
    }

    /**
     * Zeichnet den aktuellen Spielzustand in ein Bild und speichert es als PNG.
     *
     * @param s     Spiel
     * @param datei Zieldatei
     * @param karte true, wenn die Debug-Karte eingeblendet werden soll
     * @throws Exception bei Schreibfehlern
     */
    private static void speichern(Spiel s, File datei, boolean karte) throws Exception {
        Ansicht a = new Ansicht(s);
        a.setzeDebugKarte(karte);
        a.setzeHilfe(false);
        BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        a.zeichne(g, 1280, 720);
        g.dispose();
        ImageIO.write(img, "png", datei);
        System.out.println("geschrieben: " + datei);
    }
}
