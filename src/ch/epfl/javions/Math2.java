package ch.epfl.javions;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/**
 * this class contains methods that perform mathematical computations. It can't be instanciated 
 * @author Matteo Pinto 
 */

public final class Math2 {

    // prevent instanciation 
    private Math2() {}

    /**
     * restricts a value inside a given interval
     * @param min the minimum of the interval
     * @param v the value
     * @param max the maximum of the interval
     * @return v if v is in the interval, min if v is smaller than min, and max if v is greater than max
     */

    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(max >= min,
                "min should be lesser or equal than max: " + min + "/" + max);
        return Math.min(Math.max(v, min), max); //cool way to do it
    }

    /** computes the hyperbolic sine of a value
     * @param x the input value
     * @return the hyperbolic sine of x
     */
    
    public static double asinh(double x) {
        return log(x+sqrt(x*x+1)); //cool math trick 
    }

}
