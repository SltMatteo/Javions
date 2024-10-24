package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * //TODO
 */

import java.util.HexFormat;

public record RawMessage(long timeStampNs, ByteString bytes) {

    public static final int LENGTH = 14; // length of a message (in bytes)
    private static final int DF_CA_START_INDEX = 0;
    private static final int DF_CA_LENGTH = 1;
    private static final int DF_START_INDEX = 3;
    private static final int DF_LENGTH = 5;
    private static final int KNOWN_TYPE_CODE = 17;
    private static final int ICAO_START_INDEX = 1;
    private static final int ICAO_END_INDEX = 3;
    private static final int PAYLOAD_START_INDEX = 4;
    private static final int PAYLOAD_END_INDEX = 10;
    private static final int TYPE_CODE_START_INDEX = 51;
    private static final int TYPE_CODE_LENGTH = 5;

    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0, "timeStamp must be non-negative: " + timeStampNs);
        Preconditions.checkArgument(bytes.size() == LENGTH);
    }

    public static RawMessage of(long timeStampNs, byte[] bytes) {
        ByteString byteString = new ByteString(bytes);
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        if (crc.crc(bytes) != 0) return null;
//        scraped idea 
//        OneBitErrorCorrector corrector = new OneBitErrorCorrector(crc);
//        int messageCrc = crc.crc(bytes);
//        if (messageCrc != 0) {
//            corrector.correctror(bytes, messageCrc);
//        }
        return new RawMessage(timeStampNs, byteString);
    }

    public static int size(byte byte0) {
        return (Bits.extractUInt(byte0, DF_START_INDEX, DF_LENGTH) == KNOWN_TYPE_CODE) ? LENGTH : 0;
    }

    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, TYPE_CODE_START_INDEX, TYPE_CODE_LENGTH);
    }

    public int downLinkFormat() {
        return (int) bytes.bytesInRange(DF_CA_START_INDEX, DF_CA_LENGTH) >>> DF_START_INDEX;
    }

    public IcaoAddress icaoAddress() {
        long address = bytes.bytesInRange(ICAO_START_INDEX,ICAO_END_INDEX+1);
        String addressString = String.format("%06X", address);
        return new IcaoAddress(addressString);
    }

    public long payload() {
        return bytes.bytesInRange(PAYLOAD_START_INDEX,PAYLOAD_END_INDEX+1);
    }

    public int typeCode() {
        return Bits.extractUInt(payload(), TYPE_CODE_START_INDEX, TYPE_CODE_LENGTH);
    }

}


