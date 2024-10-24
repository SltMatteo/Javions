package ch.epfl.customTests;

import ch.epfl.javions.demodulation.PowerComputer;
import ch.epfl.javions.demodulation.PowerWindow;
import ch.epfl.javions.demodulation.PowerWindow;
import ch.epfl.javions.demodulation.SamplesDecoder;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomSamplesDecoderTest {

    @Test
    void testSamplesDecoder() throws IOException {
        InputStream stream = new FileInputStream("resources/samples.bin");
        SamplesDecoder decoder = new SamplesDecoder(stream, 5000);
        short[] shorts = new short[5000];
        int n = decoder.readBatch(shorts);
        //System.out.println(n);

    }

    @Test
    void testPowerComputer() throws IOException {
        InputStream stream = new FileInputStream("resources/samples.bin");
        int computerBatchSize = 4808;
        //System.out.println("on entre dans le test");
        PowerComputer computer = new PowerComputer(stream, computerBatchSize);
        int[] ints = new int[computerBatchSize];
        computer.readBatch(ints);
        for(int i : ints) {
            //System.out.println(i);
        }
    }

    @Test
    void testPowerWindow() throws IOException{
        InputStream stream = new FileInputStream("resources/samples.bin");
        PowerWindow window = new PowerWindow(stream, 1<<16);
        int get = window.get(0);
        //System.out.println("get(0, position = 0) : " + get);
        window.advance();
        window.advance();
        window.advance();
        window.advance();
        window.advance();
        get = window.get(0);
        //System.out.println("get(0, position = 4)) : " + get);
    }

}


