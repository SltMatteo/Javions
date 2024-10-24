package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * represents a decoder, that takes an input stream coming from an AirSpy and turn the incoming
 * bytes in batches of sequence of signed 12-bits values of variable sizes
 */

public final class SamplesDecoder {

    private final InputStream stream; //coming from the AirSpy
    private final int batchSize;
    private final byte[] byteReader;
    private final int BIAS = 2048;

    /**
     * creates an instance of a samples decoder decoding values from a given stream
     * and groups them in batches of given size
     * @param stream the input stream
     * @param batchSize the size of the batches we create
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0, "batch size must be positive");
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        byteReader = new byte[Short.BYTES * batchSize];
    }

    /**
     * decodes the input stream and writes the decoded values in the array given as a parameter
     * The decoder transforms a sequence of bytes into a sequence of 12-bits values in 'short' format
     * so 2 values from the AirSpy creates 1 decoded value in the batch!
     * @param batch the array we write the decoded values into
     * @return the number of samples effectively converted
     * @throws IOException in case of input/output error
     *
     * note : the method does not return the array, it only writes the values in it !
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize, "batch length must be equal to batchSize: " + batch.length + "/" + batchSize);
        int bytesRead = stream.readNBytes(byteReader, 0, batchSize*2);
        int samplesConverted = bytesRead / 2;
        for(int i = 0; i < samplesConverted; i++) {
            int b1 = Byte.toUnsignedInt(byteReader[i * 2]);
            int b2 = Byte.toUnsignedInt(byteReader[i * 2 + 1]);
            int sampleValue = ((b2 << Byte.SIZE) | b1) - BIAS;
            batch[i] = (short) sampleValue;
        }
        return samplesConverted;
    }


}
