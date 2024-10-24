package ch.epfl.customTests;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.AircraftIdentificationMessage;
import ch.epfl.javions.adsb.RawMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomAircraftIdentificationMessageTest {

    @Test
    void test() {
        var resource = getClass().getResource("/javions_tests_4.zip");

    }

    @Test

    public void test2() {
        String string = "8D3999E4234D74B3C832202700DB";
        ByteString byteString = ByteString.ofHexadecimalString(string);
        RawMessage rawMessage = new RawMessage(0, byteString);
        System.out.println(rawMessage.payload());
        System.out.println(AircraftIdentificationMessage.of(rawMessage));
    }
}
