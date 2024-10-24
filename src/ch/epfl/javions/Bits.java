package ch.epfl.javions;
// a changer

import java.util.Objects;

/**
 * this class contains methods that do bits manipulation shenanigans 
 * it can't be instanciated 
 */

public final class Bits {

    // can't be instanciated
    private Bits() {}

    /** extracts a sample of bits from a value of type long
     * The sample contains 'size' bits, and starts at the bit of
     * index 'start' of the long we are extracting from
     * @param value the long we extract the sample from
     * @param start the index of the bit we start to extract from
     * @param size the length of the sample we extract
     * @return the sequence of bits that was extracted
     * interpreted as an unsigned integer
     */

    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument((size > 0 && size < Integer.SIZE), "size must be positive and lesser than 32: " + size);
        Objects.checkFromIndexSize(start, size, Long.SIZE);
        long shiftedLeft = value << (Long.SIZE - (start + size));
        return (int) (shiftedLeft >>> (Long.SIZE - size));
    }

    /**
     * checks if a certain bit from a value is on
     * @param value the value we check the bit from
     * @param index the index of the bit we test
     * @return true iff the bit we test has value 1
     */
    
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index,Long.SIZE);
        long mask = ~(1L << index); //creates a mask with every bit on except the one at the index we are testing
        return ((value | mask) == ~0); //checks if mask AND value have only 1's
    }

}

