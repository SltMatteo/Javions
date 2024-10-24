package ch.epfl.customTests;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CprDecoder;
import org.junit.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomAirbornePositionMessageTest {

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

    @Test
    public void arrangeTest() {
        assertEquals(512, arrange(1));
        assertEquals(8, arrange(2));
        assertEquals(1024, arrange(4));
        assertEquals(16, arrange(8));
        assertEquals(2048, arrange(16));
        assertEquals(32, arrange(32));
        assertEquals(64, arrange(64));
        assertEquals(1, arrange(128));
        assertEquals(128, arrange(256));
        assertEquals(2, arrange(512));
        assertEquals(256, arrange(1024));
        assertEquals(4, arrange(2048));
    }

    public double grey(double grayCode) {
        int G_0 = (int) grayCode;
        int G = 0;
        for(int i = 0; i < 12; i++) {
            G ^= G_0 >> i;
        }
        return G;
    }

    @Test
    public void testGray() {
        assertEquals(44.0, grey(58));
        assertEquals(78.0, grey(105));
        assertEquals(32.0, grey(48));
        assertEquals(128.0, grey(192));
        assertEquals(1.0, grey(1));
    }

    public double altitude(long rawAltitude) {
        long altitudeQ = Bits.extractUInt(rawAltitude, 0, 4) |
                (Bits.extractUInt(rawAltitude, 5, 7) << 4);
        if (Bits.testBit(rawAltitude, 4)) {
            return Units.convertFrom((25 * altitudeQ) - 1000, Units.Length.FOOT);
        }
        else {
            long arrangedAltitude = (long) arrange(rawAltitude);
            double weakBits = Bits.extractUInt(arrangedAltitude, 0, 3);
            double strongBits = Bits.extractUInt(arrangedAltitude, 3, 9);
            double weakGrayBits = grey(weakBits);
            double strongGrayBits = grey(strongBits);
            if (weakGrayBits == 0 || weakGrayBits == 5 || weakGrayBits == 6) return -1;
            if (weakGrayBits == 7) weakGrayBits = 5;
            if (strongGrayBits%2 == 1) weakGrayBits = 6 - weakGrayBits;
            double truc = Units.convertFrom(strongGrayBits*500 + weakGrayBits*100 - 1300, Units.Length.FOOT);
            return Math.round(truc*100.0)/100.0;
        }
    }

    @Test
    public void testAltitude() {
        assertEquals(7315.2, altitude(1610));
    }


}
