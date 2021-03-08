package encoderDecoder;

import encoderDecoder.MessageEncoderDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectEncoderDecoder<T> implements MessageEncoderDecoder<Serializable> {

    private final byte[] lengthBytes = new byte[4];
    private int lengthBytesIndex = 0;
    private byte[] objectBytes = null;
    private int objectBytesIndex = 0;

    @Override
    public Serializable decodeNextByte(byte nextByte) {
        if (objectBytes == null) { //indicates that we are still reading the length
            lengthBytes[lengthBytesIndex++] = nextByte;
            if (lengthBytesIndex == lengthBytes.length) { //we read 4 bytes and therefore can take the length
                int len = bytesToInt(lengthBytes);
                objectBytes = new byte[len];
                objectBytesIndex = 0;
                lengthBytesIndex = 0;
            }
        } else {
            objectBytes[objectBytesIndex++] = nextByte;
            if (objectBytesIndex == objectBytes.length) {
                Serializable result = deserializeObject();
                objectBytes = null;
                return result;
            }
        }

        return null;
    }

    private static void intToBytes(int i, byte[] b) {
        b[0] = (byte) (i >> 24);
        b[1] = (byte) (i >> 16);
        b[2] = (byte) (i >> 8);
        b[3] = (byte) i;
    }

    private static int bytesToInt(byte[] b) {
        //this is the reverse of intToBytes,
        //note that for every byte, when casting it to int,
        //it may include some changes to the sign bit so we remove those by anding with 0xff

        return ((b[0] & 0xff) << 24)
                | ((b[1] & 0xff) << 16)
                | ((b[2] & 0xff) << 8)
                | (b[3] & 0xff);
    }

    @Override
    public byte[] encode(Serializable message) {
        return serializeObject(message);
    }

    private Serializable deserializeObject() {
        try {
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(objectBytes));
            return (Serializable) in.readObject();
        } catch (Exception ex) {
            throw new IllegalArgumentException("cannot deserialize object", ex);
        }

    }

    private byte[] serializeObject(Serializable message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            //placeholder for the object size
            for (int i = 0; i < 4; i++) {
                bytes.write(0);
            }

            ObjectOutput out = new ObjectOutputStream(bytes);
            out.writeObject(message);
            out.flush();
            byte[] result = bytes.toByteArray();

            //now write the object size
            intToBytes(result.length - 4, result);
            return result;

        } catch (Exception ex) {
            throw new IllegalArgumentException("cannot serialize object", ex);
        }
    }

}