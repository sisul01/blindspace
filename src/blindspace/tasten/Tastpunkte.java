package blindspace.tasten;

import blindspace.konfig.Konstanten;
import blindspace.welt.Kantenstueck;
import blindspace.welt.Oberflaeche;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Das räumliche Gedächtnis: Kurzzeitpunkte, Langzeitgruppen und Kollisionsdämpfung.
 *
 * <p>
 * Neue Tastpunkte liegen im <em>Kurzzeitgedächtnis</em> und verblassen dort bis auf den Restwert
 * {@link Konstanten#KZ_REST} (Gehen 15 s, Stehen 30 s). Danach werden sie ins <em>Langzeitgedächtnis</em>
 * überführt und je Raumzelle ({@link Konstanten#LZ_ZELLE}) und Oberflächenart zu {@link Verblassungsgruppe}n zusammengefasst, damit
 * die Zahl der zu verwaltenden Punkte klein bleibt. Gruppen verschwinden nach {@link Konstanten#LZ_DAUER}.
 * </p>
 *
 * <p>
 * Daneben verwaltet die Klasse die ertasteten {@link Tastkante}n. Von jedem Kantenstück gibt es höchstens eine
 * Instanz; eine erneute Berührung ersetzt sie durch eine frische (Auffrischen). Eine Kante durchläuft beide
 * Gedächtnisphasen selbst und wird nicht zu Gruppen zusammengefasst.
 * </p>
 */
public final class Tastpunkte {
    /**
     * Schlüssel einer Raumzelle des Langzeitgedächtnisses.
     *
     * @param x Zellenindex in X
     * @param y Zellenindex in Y
     * @param z Zellenindex in Z
     * @param oberflaeche Oberflächenart (Gruppen mischen keine Oberflächenarten)
     */
    private record Zelle(int x, int y, int z, Oberflaeche oberflaeche) {
    }

    /** Aktive Kurzzeitpunkte, ältester zuerst. */
    private final List<Tastpunkt> liste = new ArrayList<>();

    /** Gruppen des Langzeitgedächtnisses je Raumzelle. */
    private final Map<Zelle, Verblassungsgruppe> gruppen = new HashMap<>();

    /** Aktive Kantenstücke (Kurz- und Langzeitphase), je Stück höchstens eines; Einfügereihenfolge. */
    private final Map<Kantenstueck, Tastkante> kanten = new LinkedHashMap<>();

    /** Erzeugt ein leeres Gedächtnis ohne Tastpunkte und Gruppen. */
    public Tastpunkte() {
    }

    /**
     * Fügt einen Kurzzeitpunkt hinzu. Wird die Obergrenze {@link Konstanten#MAX_TASTPUNKTE} überschritten, geht der
     * älteste Punkt sofort ins Langzeitgedächtnis über.
     *
     * @param p neuer Tastpunkt
     */
    public void hinzufuegen(Tastpunkt p) {
        liste.add(p);
        while (liste.size() > Konstanten.MAX_TASTPUNKTE) {
            Tastpunkt alt = liste.remove(0);
            uebernehme(alt, p.t0);
        }
    }

    /**
     * Führt die Gedächtnisphasen nach: Kurzzeitpunkte, deren Dauer abgelaufen ist, gehen ins
     * Langzeitgedächtnis über; vollständig verblasste Gruppen werden gelöscht.
     *
     * @param zeit aktuelle Spielzeit in Sekunden
     */
    public void aufraeumen(double zeit) {
        for (Iterator<Tastpunkt> it = liste.iterator(); it.hasNext();) {
            Tastpunkt p = it.next();
            if (zeit - p.t0 >= p.kurzdauer) {
                it.remove();
                uebernehme(p, p.t0 + p.kurzdauer);
            }
        }
        gruppen.values().removeIf(g -> g.abgelaufen(zeit));
        kanten.values().removeIf(k -> k.abgelaufen(zeit));
    }

    /**
     * Meldet ein ertastetes Kantenstück. Gibt es das Stück schon, wird es durch die neue Berührung aufgefrischt
     * (Helligkeit und Zeit zurückgesetzt), allerdings höchstens alle {@link Konstanten#KANTE_ERNEUERUNG}
     * Sekunden, damit bei dauerhaftem Kontakt nicht ständig neue Objekte entstehen.
     *
     * @param k neue Berührung des Kantenstücks
     * @return true, wenn das Stück neu aufgenommen oder aufgefrischt wurde
     */
    public boolean kante(Tastkante k) {
        Tastkante alt = kanten.get(k.stueck);
        if (alt != null && k.t0 - alt.t0 < Konstanten.KANTE_ERNEUERUNG) {
            return false;
        }
        kanten.put(k.stueck, k);
        return true;
    }

    /**
     * Überführt einen Punkt ins Langzeitgedächtnis: Er wird in die Gruppe seiner Raumzelle aufgenommen oder
     * bildet eine neue Gruppe.
     *
     * @param p    Kurzzeitpunkt
     * @param zeit Zeitpunkt der Übernahme (Helligkeit wird für diesen Zeitpunkt berechnet)
     */
    private void uebernehme(Tastpunkt p, double zeit) {
        double hell = p.helligkeit(zeit);
        double r = p.durchmesser / 2;
        Zelle k = new Zelle((int) Math.floor(p.x / Konstanten.LZ_ZELLE), (int) Math.floor(p.y / Konstanten.LZ_ZELLE),
                (int) Math.floor(p.z / Konstanten.LZ_ZELLE), p.oberflaeche);
        Verblassungsgruppe g = gruppen.get(k);
        if (g == null) {
            gruppen.put(k, new Verblassungsgruppe(p.x, p.y, p.z, r, zeit, hell, p.oberflaeche));
        } else {
            g.nimmAuf(p.x, p.y, p.z, r, zeit, hell);
        }
    }

    /**
     * Meldet eine Körperkollision: Die Helligkeit aller aktiven Visualisierungen (Kurzzeitpunkte und
     * Gruppen) wird mit dem Faktor des getroffenen Körperteils multipliziert.
     *
     * @param faktor Helligkeitsfaktor (Beine 0,7, Rumpf 0,55, Kopf 0,4)
     * @param zeit   aktuelle Spielzeit in Sekunden
     */
    public void kollision(double faktor, double zeit) {
        for (Tastpunkt p : liste) {
            p.mult *= faktor;
        }
        for (Verblassungsgruppe g : gruppen.values()) {
            g.daempfe(zeit, faktor);
        }
        for (Tastkante k : kanten.values()) {
            k.mult *= faktor;
        }
    }

    /**
     * Liefert alle Kurzzeitpunkte.
     *
     * @return unveränderliche Sicht auf die Liste (ältester zuerst)
     */
    public List<Tastpunkt> alle() {
        return Collections.unmodifiableList(liste);
    }

    /**
     * Liefert alle Verblassungsgruppen des Langzeitgedächtnisses.
     *
     * @return unveränderliche Sicht auf die Gruppen (Reihenfolge nicht festgelegt)
     */
    public java.util.Collection<Verblassungsgruppe> gruppen() {
        return Collections.unmodifiableCollection(gruppen.values());
    }

    /**
     * Liefert alle aktiven Kantenstücke.
     *
     * @return unveränderliche Sicht auf die Kantenstücke (Reihenfolge nach erster Aufnahme)
     */
    public java.util.Collection<Tastkante> kanten() {
        return Collections.unmodifiableCollection(kanten.values());
    }

    /**
     * Anzahl der aktiven Kantenstücke.
     *
     * @return Anzahl
     */
    public int anzahlKanten() {
        return kanten.size();
    }

    /**
     * Anzahl der Kurzzeitpunkte.
     *
     * @return Anzahl
     */
    public int anzahl() {
        return liste.size();
    }

    /**
     * Anzahl der Verblassungsgruppen im Langzeitgedächtnis.
     *
     * @return Anzahl der Gruppen
     */
    public int anzahlGruppen() {
        return gruppen.size();
    }

    /** Entfernt alle Punkte, Gruppen und Kantenstücke (z. B. beim Neustart). */
    public void leeren() {
        liste.clear();
        gruppen.clear();
        kanten.clear();
    }
}
