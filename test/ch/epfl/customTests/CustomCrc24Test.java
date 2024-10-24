package ch.epfl.customTests;

import ch.epfl.javions.Crc24;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomCrc24Test {

    @Test
    void testAircraftDatabase() {
        IcaoAddress adress = new IcaoAddress("7C0202");
        AircraftDatabase db = new AircraftDatabase(adress.string());
    }



    @Test
    void testCrc1() {
        System.out.println("crc1");
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        String mS = "8D4B17E399893E15C09C21";
        String cS = "9FC014";
        int c = Integer.parseInt(cS,16);

        System.out.println("1");
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));
        System.out.println("2");


        byte[] mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));
        System.out.println("3");
    }

    @Test
    void isTrueTrue() {
        boolean myTrue = true;
        assertEquals(true, myTrue);
    }
}
