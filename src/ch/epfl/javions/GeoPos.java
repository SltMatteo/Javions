package ch.epfl.javions;

import static ch.epfl.javions.Units.Angle.*;
import static java.lang.Math.scalb;

/**
 * represents a geographic position (latitude, longitude) expressed in T32
 * @author Matteo Pinto 
 */

public record GeoPos(int longitudeT32, int latitudeT32) {

    /**
     * checks if a given latitude (expressed in T32) is valid, which it is iff it is between -90 degrees and +90 degrees
     * @param latitudeT32 the latitude we check
     * @return true iff the latitude is valid
     */
    
    public static boolean isValidLatitudeT32(int latitudeT32) {
        int MAX_ABSOLUTE_LATITUDE_T32 = 1 << 30;
        return(latitudeT32 >= -(MAX_ABSOLUTE_LATITUDE_T32) && latitudeT32 <= MAX_ABSOLUTE_LATITUDE_T32);
    }

    /**
     * creates an instance of a geographic position with the given longitude and latitude
     * throws an IllegalArgumentException if the latitude is not valid
     * @param longitudeT32 the longitude expressed in T32
     * @param latitudeT32 the latitude expressed in T32
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * returns the longitude of the geographic position in radians
     * @return the longitude in radians
     */
    public double longitude() {
        return Units.convert(longitudeT32, T32, RADIAN);
    }


    /**
     * returns the latitude of the geographic position in radians
     * @return the latitude in radians
     */
    public double latitude() {
        return Units.convert(latitudeT32, T32, RADIAN);
    }

    /**
     * returns a string representation of the position in the format : (longitude in degrees, latitude in degrees)
     * @return the string representation of the position
     */
    @Override
    public String toString() {
        return "(" + Units.convert(longitudeT32, T32, DEGREE) + "°, "+ Units.convert(latitudeT32, T32, DEGREE) + "°)";
    }

}


