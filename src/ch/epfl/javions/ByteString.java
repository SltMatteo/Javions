package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * the ByteString object represents a string of bytes. It is similar to byte[] but is immutable 
 * and its bytes are trated as unsigned
 */

public final class ByteString {

    byte[] bytes;

    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }


    /**
     * buils the ByteString corresponding to a string given in hexadecimal 
     * @param hexString the hexadecimal representation of the string 
     * @return the ByteString correspondance 
     */
    public static ByteString ofHexadecimalString(String hexString) {
        if(hexString.length()%2!=0) {
            throw new IllegalArgumentException("hexString should have an even number of chars: " + hexString);
        }

        HexFormat hf = HexFormat.of().withUpperCase();
        return new ByteString(hf.parseHex(hexString));
    }

    /**
     * returns the size (in number of bytes) of the ByteString
     * @return size in bytes 
     */
    public int size() {
        return bytes.length;
    }

    /**
     * returns the byte at the given index 
     * @param index the index of the byte 
     * @return the byte at that location 
     */
    public int byteAt(int index) {
        if(index>bytes.length+1) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds for the ByteString " + this.toString());
        }

        return Byte.toUnsignedInt(bytes[index]);
    }
    /**
     * returns the bytes between two indices
     * @param fromIndex the starting index (included) 
     * @param toIndex the endind index (excluded) 
     * @return the sequence of bytes between the indices, treated as a long 
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
        Preconditions.checkArgument((toIndex - fromIndex < 8));
        long res = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            res = ((res << 8) | Byte.toUnsignedInt(bytes[i]));
        }
        return res;
    }

    @Override
    public boolean equals(Object thatO) {
        return (thatO instanceof ByteString that);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        HexFormat hf = HexFormat.of().withUpperCase();
        return hf.formatHex(bytes);
    }



}