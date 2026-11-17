package blindspace.test;

import blindspace.anzeige.Ansicht;
import blindspace.spiel.Spiel;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Normal spielbares Spiel, aber mit eingeblendeter Debug-Draufsicht (Welt, Körper, Hände).
 * Start: Rechtsklick -> Run As -> Java Application. Die Karte lässt sich mit F3 aus- und einblenden.
 */
public final class SpielMitKarte {
    /** Startklasse, keine Instanzen. */
    private SpielMitKarte() {
    }

    /**
     * Startet das Testlevel mit eingeblendeter Debug-Karte.
     *
     * @param args Kommandozeilenargumente (nicht ausgewertet)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Spiel spiel = new Spiel();
            Ansicht ansicht = new Ansicht(spiel);
            ansicht.setzeDebugKarte(true);
            ansicht.setPreferredSize(new Dimension(1280, 720));

            JFrame fenster = new JFrame("Blindspace – Test mit Debug-Karte (F3 schaltet die Karte um)");
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
