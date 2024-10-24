package ch.epfl.javions;
import java.lang.Math;

import static java.lang.Math.*;

/**
 * this class contains the definition of units in the International System (SI)
 * and methods that do conversions between them
 * The base unit for each measure (eg: meter for distances, seconds for times) have a value of 1
 * and all other measures are expressed in terms of the base unit
 * this class can't be instanciated 
 * @author Matteo Pinto 
 */

public final class Units {

    // prevent instantiation 
    private Units() {}

    public static final double CENTI = 1e-2;
    public static final double KILO = 1e3;

    /**
     * contains the values of the constants radians, turn,
     * degree, and t32, all expressed in radians
     */
    public static final class Angle {
        private Angle() {}
        public static final double RADIAN = 1;
        public static final double TURN = RADIAN*2*PI;
        public static final double DEGREE = TURN / 360;
        public static final double T32 = scalb(TURN, -32);
    }

    /**
     * contains the values of the constants meter, centimeter, kilometer,
     * inch, foot, nautical mile, all expressed in meters
     */
    public static final class Length {
        private Length() {}
        public static final double METER = 1;
        public static final double CENTIMETER = CENTI*METER;
        public static final double KILOMETER = KILO*METER;
        public static final double INCH = 2.54*CENTIMETER;
        public static final double FOOT = 12*INCH;
        public static final double NAUTICAL_MILE = 1852*METER;
    }

    /**
     * contains the value of the constants second, minute, hour,
     * all expressed in seconds
     */
    public static final class Time {
        private Time() {}
        public static final double SECOND = 1;
        public static final double MINUTE = 60*SECOND;
        public static final double HOUR = 60*MINUTE;
    }

    /**
     * contains the value of the constants meter per second,
     * kilometer per hour, knot, all expressed in meter per seconds
     */
    public static final class Speed {
        private Speed() {}
        public static final double METER_PER_SECOND = 1;
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
    }

    /**
     * converts a given value from one unit to another
     * @param value the value expressed in terms of the starting unit
     * @param fromUnit the starting unit
     * @param toUnit the ending unit
     * @return the value in terms of the ending unit
     *
     * eg: convert(1000, Length.METER, LENGTH.KILOMETER) returns 1
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return ((value*fromUnit)/toUnit);


    }

    /** perform the conversion of a given value from a given unit to the base unit
     * meaning it expresses the given value in terms of the base unit
     * @param value the value expressed in terms of the starting unit
     * @param fromUnit the starting unit
     * @return the value expressed in the base unit
     *
     * eg: convertFrom(100, Length.CENTIMETER) returns 1
     * note: this method is equivalent to the 'convert' method iff toUnit is the base unit
     */

    public static double convertFrom(double value, double fromUnit) {
        return value*fromUnit;
    }

    /** perform the conversion of a given value from the base unit to a given unit
     * meaning it expresses the given value (expressed in the base unit) in terms of the target unit
     * @param value the value expressed in terms of the base unit
     * @param toUnit the target unit
     * @return the value expressed in the target unit
     *
     * eg: convertFrom(1, Length.CENTIMETER) returns 100
     * note: this method is equivalent to the 'convert' method iff fromUnit is the base unit
     */
    public static double convertTo(double value, double toUnit) {
        return value/toUnit;
    }
}

