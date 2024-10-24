package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;
import java.io.IOException;
import java.io.InputStream;

/**
 * represents a power computer, an object that turn values from a samples decoder into
 * batches of power samples (motivation behind this : mathematical stuff), values that
 * will have effective meaning later
 */

public final class PowerComputer {

    private int batchSize;
    private short[] currentValues;
    private short[] batchSamples;
    private SamplesDecoder decoder;

    /**
     * creates an instance of a power computer that will decode values from a given
     * stream and creates batches of a given size
     * takes a stream in input and not an array of decoded samples
     * because it's the computer himself that creates the instance of a samples decoder
     * used to decode the values of the stream
     * @param stream the input stream
     * @param batchSize the batch size
     */

    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(((batchSize%8)==0) && batchSize>0);
        this.batchSize = batchSize;
        batchSamples = new short[2*batchSize];
        currentValues = new short[8];
        decoder = new SamplesDecoder(stream, batchSize*2);
        for(int i = 0; i < 8; i++) {
            currentValues[i] = 0;
        }
    }

    /**
     * compute the values of power samples and write them in the given array
     * @param batch the array we write the power samples into
     * @return the number of values effectively computed and written in the batch
     * @throws IOException in case of input/output error
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == this.batchSize);
        int samples = decoder.readBatch(batchSamples) / 2;
        for(int i = 0; i < batch.length; i++) {
            currentValues = rotate(currentValues, i, batchSamples);
            batch[i] = (int) (Math.pow(currentValues[6]-currentValues[4]+currentValues[2]-currentValues[0], 2)
                    + Math.pow(currentValues[7]-currentValues[5]+currentValues[3]-currentValues[1], 2));
        }
        return samples;
    }

    /**
     * shifts an array two indexes forward, and write the two next values
     * of the samples decoder in the two newly freed spots
     * @param toBeRotated the array we rotate
     * @param step used to see which values of the samples decoder we need to insert
     * @param valuesToPut array containing the values we need to insert in the array
     * @return the shifted array with the two next values of the decoder
     */
    private short[] rotate(short[] toBeRotated, int step, short[] valuesToPut) {
        short[] rotated = new short[8]; // size 8 because the computer needs 8 values to compute 1 power sample
        for(int i = 7; i > 1; i--) {
            rotated[i] = toBeRotated[i-2];
        }
        rotated[0] = valuesToPut[(2*step)];
        rotated[1] = valuesToPut[((2*step)+1)];
        return rotated;
    }

}
