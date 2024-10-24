package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.Objects;

/**
 * //TODO
 * @param timeStampNs
 * @param icaoAddress
 * @param category
 * @param callSign
 * @author Matteo Pinto (326649)
 */

public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message {

    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0, "time stamp must be non neagative: " + timeStampNs);
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
    }

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        int typeCode = 14 - rawMessage.typeCode();
        int partialCategory = Bits.extractUInt(payload, 48, 3);
        int category = (typeCode << 4) | partialCategory;
        int c;
        char actual;
        StringBuilder callSignStringBuilder = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            c = Bits.extractUInt(payload, 6*i, 6);
            if (c >= 1 && c <= 26) {
                actual = (char) ('A' + (c - 1));
                callSignStringBuilder.insert(0,actual); //inserts are slow -> instead append + reverse
            }
            else if (c >= 48 && c <= 57) {
                actual = (char) ('0' + (c - 48));
                callSignStringBuilder.insert(0,actual);

            }
            else if (c == 32) {
                actual = ' ';
                callSignStringBuilder.insert(0,actual);
            }
            else {
                return null;
            }
        }

        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        CallSign callSign = new CallSign(callSignStringBuilder.toString().trim()); //trim
        return new AircraftIdentificationMessage(timeStampNs, icaoAddress, category, callSign);
    }
}
