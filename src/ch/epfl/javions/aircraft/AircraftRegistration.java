package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;
import java.util.regex.Pattern;

/**
 * represents the registration number of an airplane
 * @param string the string representation of the registration number
 * @author Moyang Zhang (355928)
 */

public record AircraftRegistration(String string) {

    private final static Pattern REG = Pattern.compile("[A-Z0-9 .?/_+-]+"); // the pattern a reg must match

    /**
     * creates an instance of an aircraft's registration code with the code given as argument
     * @throws IllegalArgumentException if the code is not valid or null
     * @param string the code we give as an argument
     */

    public AircraftRegistration {
        Preconditions.checkArgument(!string.isEmpty());
        Preconditions.checkArgument(REG.matcher(string).matches());
    }
}
