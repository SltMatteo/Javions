package ch.epfl.javions.aircraft;

/**
 * values the wake turbulence attribute of an airplane can take
 * @author Matteo Pinto (326649)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    public static WakeTurbulenceCategory of(String s) { //enhanced switch directly returns the whole expression (cleaner)
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}
