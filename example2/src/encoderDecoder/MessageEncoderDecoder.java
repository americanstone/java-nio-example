package encoderDecoder;

/*
The MessageEncoderDecoder interface is a filter that we put between the Socket input stream and the protocol.
 The protocol does not access the input/output stream directly - it only handle application level messages while
 the MessageEncoderDecoder responsible to translate them to and from bytes.
 This way, one can use the same protocol under different message formats and reuse message formats for different protocols.
 */
public interface MessageEncoderDecoder<T> {

    /**
     * add the next byte to the decoding process
     *
     * @param nextByte the next byte to consider for the currently decoded message
     * @return a message if this byte completes one or null if it doesnt.
     */
    T decodeNextByte(byte nextByte);

    /**
     * encodes the given message to bytes array
     * @param message the message to encode
     * @return the encoded bytes
     */
    byte[] encode(T message);

}