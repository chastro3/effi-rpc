package io.effi.rpc.util.bytes;

import java.nio.charset.Charset;

/**
 * Tool for Reading bytes.
 */
public interface ByteReader {

    /**
     * Read a byte.
     */
    byte readByte();

    /**
     * Read a boolean value.
     */
    boolean readBoolean();

    /**
     * Read a short value.
     */
    short readShort();

    /**
     * Read an int value.
     */
    int readInt();

    /**
     * Read a long value.
     */
    long readLong();

    /**
     * Read a float value.
     */
    float readFloat();

    /**
     * Read a double value.
     */
    double readDouble();

    /**
     * Read a string, the length of which is specified by the parameter length.
     *
     * @param length
     */
    CharSequence readCharSequence(int length);

    /**
     * Read a string, the length of which is specified by the parameter length and the charset character set.
     *
     * @param length
     * @param charset
     */
    CharSequence readCharSequence(int length, Charset charset);

    /**
     * Read a byte array, the length of which is specified by the parameter length.
     *
     * @param length
     */
    byte[] readBytes(int length);

    /**
     * The number of readable bytes.
     */
    int readableBytes();

    /**
     * Read all remaining bytes.
     */
    default byte[] readRemainingBytes() {
        return readBytes(readableBytes());
    }

}