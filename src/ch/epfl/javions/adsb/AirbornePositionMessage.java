package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.Objects;

/**
 * Represents a position message sent by an airplane, tells the receiver the altitude and the position of the sender
 * @param timeStampNs the time at which the message was sent
 * @param icaoAddress the address of the sender
 * @param altitude the altitude of the sender
 * @param parity the parity of the message
 * @param x the longitude of the sender
 * @param y the latitude of the sender
 * @author Matteo Pinto (326649)
 */

public record AirbornePositionMessage (long timeStampNs, IcaoAddress icaoAddress, double altitude,
                                      int parity, double x, double y) implements Message {

    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0,"time stamp must be non negative: " + timeStampNs);
        Preconditions.checkArgument(parity == 0 || parity == 1, "parity mist be 0 or 1: " + parity);
        Preconditions.checkArgument(x >= 0 && x < 1, String.valueOf(x));
        Preconditions.checkArgument(y >= 0 && y < 1, String.valueOf(y));
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * extract the information of a raw message and create an instance of a position message with the information extracted
     * @param rawMessage the raw message sent
     * @return the position message interpreted
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        long rawAltitude = Bits.extractUInt(rawMessage.payload(), 36, 12);
        double altitude = altitude(rawAltitude);
        if (altitude == -1) return null;
        int parity = Bits.extractUInt(payload, 34, 1);
        double x = (double) Bits.extractUInt(payload, 0, 17) / (1 << 17);
        double y = (double) Bits.extractUInt(payload, 17, 17) / (1 << 17);

        return new AirbornePositionMessage(timeStampNs, icaoAddress, altitude, parity, x, y);
    }

    /**
     * computes the altitude of the sender
     * @param rawAltitude the altitude (encoded)
     * @return the altitude in meters
     */
    private static double altitude(long rawAltitude) {
        long altitudeQ = Bits.extractUInt(rawAltitude, 0, 4) | ((long) Bits.extractUInt(rawAltitude, 5, 7) << 4);
        if (Bits.testBit(rawAltitude, 4)) {
            return Units.convertFrom((25 * altitudeQ) - 1000, Units.Length.FOOT);
        }
        else {
            long arrangedAltitude = (long) arrange(rawAltitude);
            double weakBits = Bits.extractUInt(arrangedAltitude, 0, 3);
            double strongBits = Bits.extractUInt(arrangedAltitude, 3, 9);
            double weakGrayBits = interpretGrayCode(weakBits);
            double strongGrayBits = interpretGrayCode(strongBits);
            if (weakGrayBits == 0 || weakGrayBits == 5 || weakGrayBits == 6) return -1;
            if (weakGrayBits == 7) weakGrayBits = 5;
            if (strongGrayBits%2 == 1) weakGrayBits = 6 - weakGrayBits;
            return Units.convertFrom(strongGrayBits*500 + weakGrayBits*100 - 1300, Units.Length.FOOT);
        }
    }

    /**
     * rearrange the bits in a 12-bit sequence following a scheme used when transmitting an altitude
     * @param altitude the 'mixed' value
     * @return the unscrambled value
     */
    private static double arrange(double altitude) {
        byte[] unarranged = new byte[12];
        for (int i = 0; i < 12; i++) {
            unarranged[i] = (byte) ((Bits.testBit((long) altitude, i)) ? 1 : 0);
        }
        return unarranged[0] << 9 |
                unarranged[1] << 3 |
                unarranged[2] << 10 |
                unarranged[3] << 4 |
                unarranged[4] << 11 |
                unarranged[5] << 5 |
                unarranged[6] << 6 |
                unarranged[7] << 0 |
                unarranged[8] << 7 |
                unarranged[9] << 1 |
                unarranged[10] << 8 |
                unarranged[11] << 2;
    }

    /**
     * interprets a value written in gray code into a normal value
     * @param grayCode the gray code format of the value
     * @return the value
     */
    private static double interpretGrayCode(double grayCode) {
        int G_0 = (int) grayCode;
        int G = 0;
        for(int i = 0; i < 12; i++) {
            G ^= G_0 >> i;
        }
        return G;
    }
}
