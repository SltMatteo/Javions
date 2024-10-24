package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;
import java.util.regex.Pattern;

/**
 * represents the description of an airplane
 * @param string the string representation of the description
 * @author Moyang Zhang (355928)
 */


public record AircraftDescription(String string) {

    private final static Pattern DESC = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]"); // the pattern a desc must match

    /**
     * creates an instance of an airplane's description with the description given in argument as a string
     * @throws IllegalArgumentException if the description is not valid or null
     * @param string the description we give as an argument
     */

    public AircraftDescription {
        Preconditions.checkArgument(string.isEmpty()||DESC.matcher(string).matches());
    }
}
