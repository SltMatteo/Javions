package ch.epfl.customTests;

import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.demodulation.AdsbDemodulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomRawMessageTest {

    //4

    public static void main(String[] args) throws IOException {
        String f = "/Users/matteo/Documents/javions/javions skeleton/Javions/resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while((m = d.nextMessage()) != null) { //finir nextMessage()
                System.out.println(m);
            }
        }
    }
}
