package ch.epfl.javions;

/**
 * this class contains method that project geographic coordinates in the Mercator System
 * It cannot be instanciated
 * @author Matteo Pinto 
 */

public final class WebMercator {

    // prevent instantiation
    private WebMercator() {}

    /**
     * computes the x coordinate in the Mercator system for a given longitude and zoom level 
     * at a given longitude (in radians) at a given zoom level
     * @param zoomLevel the zoom level
     * @param longitude the longitude of the point
     * @return the x coordinate in the Mercator System
     */

    public static double x(int zoomLevel, double longitude) {
        return ((Math.pow(2,zoomLevel + 8))*(Units.convertTo(longitude,Units.Angle.TURN) + 0.5));
    }

    /**
     * compute the y coordinate in the Mercator system for a given longitude and zoom level 
     * at a given latitude (in radians) at a given zoom level
     * @param zoomLevel the zoom level
     * @param latitude the longitude of the point
     * @return the y coordinate in the Mercator System
     */

    public static double y(int zoomLevel, double latitude) {
        return (Math.pow(2,zoomLevel + 8)*(Units.convertTo(Math2.asinh(Math.tan(latitude)),-Units.Angle.TURN) + 0.5));
    }
}
