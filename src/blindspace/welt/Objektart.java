package blindspace.welt;

/**
 * Objektkategorie (GDD Abschnitt 3). Sie ist die Grundlage für spätere Interaktionen und Etagen: Türen,
 * Schalter, Schlüssel und Treppen brauchen ein eigenes Verhalten, das über den Typ zugeordnet wird.
 *
 * <p>
 * Bisher hat nur der Typ selbst Bedeutung (Beschriftung und Tests); die Kategorien ab {@link #TUER} sind
 * vorbereitet, aber noch ohne Verhalten.
 * </p>
 */
public enum Objektart {
    /** Der virtuelle Boden. */
    BODEN,

    /** Wand oder Wandvorsprung. */
    WAND,

    /** Säule oder freistehender Pfeiler. */
    SAEULE,

    /** Möbel und Einrichtungsgegenstände. */
    MOEBEL,

    /** Sonstiges Hindernis ohne besondere Bedeutung. */
    SONSTIGES,

    /** Tür, Türrahmen (vorbereitet, noch ohne Verhalten). */
    TUER,

    /** Türgriff (vorbereitet, noch ohne Verhalten). */
    GRIFF,

    /** Schalter (vorbereitet, noch ohne Verhalten). */
    SCHALTER,

    /** Aufnehmbarer Gegenstand, z. B. Schlüssel (vorbereitet, noch ohne Verhalten). */
    GEGENSTAND,

    /** Treppe, Stufe oder Podest (vorbereitet, noch ohne Verhalten). */
    TREPPE,

    /** Geländer (vorbereitet, noch ohne Verhalten). */
    GELAENDER
}
