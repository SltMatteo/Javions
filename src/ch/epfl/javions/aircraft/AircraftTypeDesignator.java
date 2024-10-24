package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;


/**
 * represents the type designator of an airplane
 * @param string the string representation of the type designator
 * @author Moyang Zhang (355928)
 */
public record AircraftTypeDesignator(String string) {

    private final static Pattern TYPE = Pattern.compile("[A-Z0-9]{2,4}"); // the pattern a type designator must match

    /**
     * creates an instance of an airplane's type designator with the designator given in argument
     * @throws IllegalArgumentException if the designator is not valid or null
     * @param string the type designator we give as an argument
     */

    public AircraftTypeDesignator {
        Preconditions.checkArgument(string.isEmpty()||TYPE.matcher(string).matches());
    }
}
