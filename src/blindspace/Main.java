package blindspace;

import blindspace.anzeige.Ansicht;
import blindspace.spiel.Spiel;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Startpunkt des Spiels: öffnet das Fenster und betreibt die Spielschleife (etwa 60 Bilder pro Sekunde).
 */
public final class Main {

    /** Startklasse, keine Instanzen. */
    private Main() {
    }

    /**
     * Startet das Spiel mit dem Testlevel.
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Spiel spiel = new Spiel();
            Ansicht ansicht = new Ansicht(spiel);
            ansicht.setPreferredSize(new Dimension(1280, 720));

            JFrame fenster = new JFrame("Blindspace – Steuerungs- und Visualisierungsprototyp");
            fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenster.setContentPane(ansicht);
            fenster.pack();
            fenster.setLocationRelativeTo(null);
            fenster.setVisible(true);
            ansicht.requestFocusInWindow();

            final long[] letzte = { System.nanoTime() };
            new Timer(16, e -> {
                long jetzt = System.nanoTime();
                double dt = (jetzt - letzte[0]) / 1e9;
                letzte[0] = jetzt;
                spiel.aktualisiere(dt);
                ansicht.repaint();
            }).start();
        });
    }
}
