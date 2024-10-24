package ch.epfl.customTests;

import ch.epfl.javions.ByteString;
import org.junit.jupiter.api.Test;

public class CustomByteStringTest {

    @Test
    void randomTest() {
        // tester avec 0 en début de string !!!!!!!!!!!!!!!!!!!!!!!!!
        var byteString = ByteString.ofHexadecimalString("1a2b3c4d5e6f1b2c3d4e5f6a");
        System.out.println(byteString);
        System.out.println(Integer.toHexString(byteString.byteAt(4)));
        System.out.println(byteString.size());
        System.out.println(byteString);
        System.out.println("--");
        System.out.println(Long.toHexString(byteString.bytesInRange(5,12)).toUpperCase());
    }
}
