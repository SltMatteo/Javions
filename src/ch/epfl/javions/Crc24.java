package ch.epfl.javions;

/** this class contains methods that perform the 24-bit cylic redundacy check (CRC24) checksum
 */

public final class Crc24 {

    public final static int GENERATOR = 0xFFF409;
    private final static int CRC_BASE = 24;
    private final int generator;
    private final byte[] table;

    public Crc24(int generator) {
        this.generator = generator;
        table = buildTable(generator);
    }

    /**
     * returns the crc24 of a squence of bits
     * @param bytes the sequence of bits
     * @return the crc24 of the given sequence
     */

    public int crc(byte[] bytes) {
        return crc_bitwise(bytes, generator);
    }

    /**
     * computes the crc24 of a sequence of bits using a given generator by applying a bitwise crc algorithm
     * @param bytes the sequence of bit we want the crc24 of
     * @param generator the generator used
     * @return the crc24 of the given value
     */

    private static int crc_bitwise(byte[] bytes, int generator) {
        generator |= (1 << CRC_BASE);
        int crc = 0;
        int[] table = {0, generator};
        for (byte aByte : bytes) {
            for (int j = 7; j >= 0; j--) {
                int bit = ((1 << j) & aByte) >>> j;
                crc = ((crc << 1) | bit) ^ table[Bits.extractUInt(crc, CRC_BASE - 1, 1)];
            }
        }
        for (int i = 0; i < 24; i++) {
            crc = (crc << 1);
            if (Bits.testBit(crc, CRC_BASE)) crc = crc ^ generator;
        }

        return Bits.extractUInt(crc, 0, CRC_BASE); //only return the 24 low weight bits
    }

    /**
     * builds the table used in the optimized crc24 algorithm
     * @param generator the generator
     * @return the table needed in the optimized algorithm
     */

    private static byte[] buildTable(int generator) {
        byte[] table = new byte[256];
        for (int i = 0; i < 256; i++) {
            byte[] cool = {(byte) i};
            table[i] = (byte) crc_bitwise(cool, generator); // can be changed to crc_optimized if more efficiency is needed
        }
        return table;
    }

    /**
     * computes the crc24 of a sequence of bits using an optimized (-ish) algorithm
     * @param bytes the sequence of bits
     * @return the crc24 of the sequence
     */

    private int crc_optimized(byte[] bytes) {
        int N = CRC_BASE;
        int crc = 0;
        byte[] table = this.table;
        for (byte b : bytes) {
            crc = ((crc << 8) | Byte.toUnsignedInt(b)) ^ (table[Bits.extractUInt(crc, N - 8, 8)]);
        }
        for (int i = 0; i < 3; i++) {
            byte b = 0;
            crc = (crc << 8) ^ (table[Bits.extractUInt(crc, N - 8, 8)]);
        }

        return Bits.extractUInt(crc, 0, CRC_BASE); //only return the 24 low weight bits
    }


}