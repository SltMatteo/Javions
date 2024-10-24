package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;
import java.util.regex.Pattern;

/**
 * represents the icao address of an airplane
 * @param string the string representation of the address
 * @author Moyang Zhang (355928)
 */

public record IcaoAddress(String string) {

    private final static Pattern OACI = Pattern.compile("[0-9A-F]{6}"); //the pattern a valid icao address must match


    /**
     * creates an instance of an airplane's ICAO address with the address given in argument as a string
     * @throws IllegalArgumentException if the address is not valid or null
     * @param string the address we give as an argument
     */

    public IcaoAddress {
        Preconditions.checkArgument(OACI.matcher(string).matches());
        Preconditions.checkArgument(!string.isEmpty());
    }
}
