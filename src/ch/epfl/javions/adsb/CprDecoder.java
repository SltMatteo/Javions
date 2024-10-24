package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import java.lang.Double;

/**
 * //TODO
 * @author Matteo Pinto (326649)
 */

public class CprDecoder {

    private static final double LATITUDE_Z_0 = 60;

    private static final double LATITUDE_Z_1 = 59;

    private static final double LATITUDE_DELTA_0 = 1 / LATITUDE_Z_0;

    private static final double LATITUDE_DELTA_1 = 1 / LATITUDE_Z_1;

    private static final int EVEN_MESSAGE = 0;
    private static final int ODD_MESSAGE = 1;

    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {

        Preconditions.checkArgument(mostRecent == EVEN_MESSAGE | mostRecent == ODD_MESSAGE);

        // step 01 : compute latitude

        double latitude_z = Math.rint((y0*LATITUDE_Z_1) - (y1*LATITUDE_Z_0));
        double latitude_z_0 = (latitude_z < 0) ? latitude_z + LATITUDE_Z_0 : latitude_z;
        double latitude_z_1 = (latitude_z < 0) ? latitude_z + LATITUDE_Z_1 : latitude_z;

        double latitude_0 = LATITUDE_DELTA_0 * (latitude_z_0 + y0); //expressed in turns and always > 0
        double latitude_1 = LATITUDE_DELTA_1 * (latitude_z_1 + y1); //expressed in turns and always > 0

        // step 02 : compute longitude zones

        int longitudeZones_0 = longitudeZones(latitude_0); // = Z_lambda_0
        int otherLongitudeZones_0 = longitudeZones(latitude_1);

        if (longitudeZones_0 != otherLongitudeZones_0) {
            return null;
        }

        int longitudeZones_1 = longitudeZones_0 - 1; // = Z_lambda_1

        // step 03 : compute longitude

        double longitude_0;
        double longitude_1;

        if (longitudeZones_0 == 1) {
            longitude_0 = x0; // expressed in turns
            longitude_1 = x1; // expressed in turns
        } else {
            double longitude_z = Math.rint((x0*longitudeZones_1) - (x1*longitudeZones_0));
            double longitude_z_0 = (longitude_z < 0) ? longitude_z + longitudeZones_0 : longitude_z;
            double longitude_z_1 = (longitude_z < 0) ? longitude_z + longitudeZones_1 : longitude_z;
            longitude_0 = (1 / (double) longitudeZones_0) * (longitude_z_0 + x0); // expressed in turns
            longitude_1 = (1 / (double) longitudeZones_1) * (longitude_z_1 + x1); // expressed in turns

        }

        // step 04 : geoPos

        switch (mostRecent) {
            case (EVEN_MESSAGE) -> {
                longitude_0 = centerAngle(longitude_0);
                latitude_0 = centerAngle(latitude_0);

                if (latitude_0 > 90 || latitude_0 < -90) {
                    return null;
                }

                int geoPosLongitude0 = (int) Units.convert(longitude_0, Units.Angle.TURN, Units.Angle.T32);
                int geoPosLatitude0 = (int) Units.convert(latitude_0, Units.Angle.TURN, Units.Angle.T32);

                if (!GeoPos.isValidLatitudeT32(geoPosLatitude0)) {
                    return null;
                }
                return new GeoPos(geoPosLongitude0, geoPosLatitude0);
            }
            case (ODD_MESSAGE) -> {
                longitude_1 = centerAngle(longitude_1);
                latitude_1 = centerAngle(latitude_1);
                if (latitude_1 > 90 || latitude_1 < -90) {
                    return null;
                }

                int geoPosLongitude1 = (int) Units.convert(longitude_1, Units.Angle.TURN, Units.Angle.T32);
                int geoPosLatitude1 = (int) Units.convert(latitude_1, Units.Angle.TURN, Units.Angle.T32);

                if (!GeoPos.isValidLatitudeT32(geoPosLatitude1)) {
                    return null;
                }
                return new GeoPos(geoPosLongitude1, geoPosLatitude1);
            }
            default -> {
                throw new IllegalArgumentException(funnyCat());
            }
        }

    }

    /**
     * computes how many longitude zones a given latitude should use
     * @param latitude the latitude we're at
     * @return the number of longitude zones we use later in the process
     */
    private static int longitudeZones(double latitude) { // longitudeZones = Z_lambda_0
        latitude = Units.convert(latitude, Units.Angle.TURN, Units.Angle.RADIAN);
        double numerator = 1 - Math.cos(2*Math.PI*LATITUDE_DELTA_0);
        double denominator = Math.pow(Math.cos(latitude), 2);
        double a = Math.acos(1 - (numerator / denominator));
        if (Double.isNaN(a)) {
            return 1;
        }
        return (int) Math.floor((2*Math.PI) / a);
    }

    /**
     * returns the negative equivalent of an angle if it is over one half turn
     * @param angle the angle we center
     * @return the angle in the smallest value format
     */
    private static double centerAngle(double angle) {
        return (angle >= (Units.Angle.RADIAN/2)) ? (angle - Units.Angle.RADIAN) : angle;
    }


    /**
     * draws a cute little fellow
     * art by Rowan Crawford, found on https://www.asciiart.eu/animals/cats
     */
    private static String funnyCat() {
        return ("           .               ,.\n" +
                "          T.\"-._..---.._,-\"/|\n" +
                "          l|\"-.  _.v._   (\" |\n" +
                "          [l /.'_ \\; _~\"-.`-t\n" +
                "          Y \" _(o} _{o)._ ^.|\n" +
                "          j  T  ,--.  T  ]\n" +
                "          \\  l ( /-^-\\ ) !  !\n" +
                "           \\. \\.  \"~\"  ./  /c-..,__\n" +
                "             ^r- .._ .- .-\"  `- .  ~\"--.\n" +
                "              > \\.                      \\\n" +
                "              ]   ^.                     \\\n" +
                "              3  .  \">            .       Y\n" +
                " ,.__.--._   _j   \\ ~   .         ;       |\n" +
                "(    ~\"-._~\"^._\\   ^.    ^._      I     . l\n" +
                " \"-._ ___ ~\"-,_7    .Z-._   7\"   Y      ;  \\        _\n" +
                "    /\"   \"~-(r r  _/_--._~-/    /      /,.--^-._   / Y\n" +
                "    \"-._    '\"~~~>-._~]>--^---./____,.^~        ^.^  !\n" +
                "        ~--._    '   Y---.                        \\./\n" +
                "             ~~--._  l_   )                        \\\n" +
                "                   ~-._~~~---._,____..---           \\\n" +
                "                       ~----\"~       \\\n" +
                "                                      \\\n");
    }
}
