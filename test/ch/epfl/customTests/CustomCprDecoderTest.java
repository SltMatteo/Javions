package ch.epfl.customTests;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.CprDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomCprDecoderTest {

    @Test
    public void decoderTest() {
        double x0 = Math.scalb(111600, -17);
        double x1 = Math.scalb(108865, -17);
        double y0 = Math.scalb(94445, -17);
        double y1 = Math.scalb(77558, -17);
        GeoPos pos0 = CprDecoder.decodePosition(x0, y0, x1, y1, 0);
        System.out.println(pos0);
        GeoPos pos1 = CprDecoder.decodePosition(x0, y0, x1, y1, 1);
        System.out.println(pos1);
    }

    @Test
    public void casLimite() {
        GeoPos posNull = CprDecoder.decodePosition(-1, -1, -1, -1, 1);
        System.out.println(posNull);
    }
}
