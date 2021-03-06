package reactor;

import encoderDecoder.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
/*
    The first thing that we should notice in the NonBlockingConnectionHandler class is the BUFFER_POOL variable.
     When reading data from nio-channels it is recommended (for performance reasons) to use direct byte buffers
     (which reside outside of the garbage collector region and therefore one can simply pass pointer to them to
     the operation system to fill on read request). @NonBlockingConnectionHandler@s uses many of such buffers for
     a short period of time, since the creation/eviction cycle of a direct bytebuffer of the needed size is relatively
     costly operation, the BUFFER_POOL will cache the already created buffers for reuse.
     This concept follows the Flyweight design-pattern.
 */
public class NonBlockingConnectionHandler<T> {
    private static final int BUFFER_ALLOCATION_SIZE = 1 << 13; //8k
    // flyweight pattern pool of re-usable buffer for channel to use
    //uses many of such buffers for a short period of time, since the creation/eviction cycle of a direct bytebuffer of the needed size is relatively costly operation,the
    //BUFFER_POOL will cache the already created buffers for reuse
    private static final ConcurrentLinkedQueue<ByteBuffer> BUFFER_POOL = new ConcurrentLinkedQueue<>();

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    // ready read event trigger, server read data from client stock and write this queue
    // ready write event trigger, server poll the buffer from queue write to client socket
    // active object pattern
    private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();

    private final SocketChannel chan; // the socket wrapper

    private final Reactor reactor;

    public NonBlockingConnectionHandler(
            MessageEncoderDecoder<T> reader,
            MessagingProtocol<T> protocol,
            SocketChannel chan,
            Reactor reactor) {
        this.chan = chan;
        this.encdec = reader;
        this.protocol = protocol;
        this.reactor = reactor;
    }

    // read the data from client stocket
    public Runnable continueRead() {
        ByteBuffer buf = leaseBuffer();

        boolean success = false;
        try {
            success = chan.read(buf) != -1;
        } catch (ClosedByInterruptException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (success) { // read data from client stock success, put data in
            buf.flip();
            return () -> {
                try {
                    while (buf.hasRemaining()) {
                        T nextMessage = encdec.decodeNextByte(buf.get());
                        if (nextMessage != null) {
                            T response = protocol.process(nextMessage);
                            if (response != null) {
                                writeQueue.add(ByteBuffer.wrap(encdec.encode(response)));
                                reactor.updateInterestedOps(chan, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            }
                        }
                    }
                } finally {
                    releaseBuffer(buf);
                }
            };
        } else {
            releaseBuffer(buf);
            close();
            return null;
        }
    }

    public void close() {
        try {
            chan.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // write data to client stocket
    public void continueWrite() {
        while (!writeQueue.isEmpty()) {
            try {
                ByteBuffer top = writeQueue.peek(); //Retrieves, but does not remove
                chan.write(top);
                if (top.hasRemaining()) {
                    return;
                } else {
                    writeQueue.remove();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                close();
            }
        }

        if (protocol.shouldTerminate()) {
            close();
        } else {
            reactor.updateInterestedOps(chan, SelectionKey.OP_READ);
        }
    }
    private static ByteBuffer leaseBuffer() {
        ByteBuffer buff = BUFFER_POOL.poll();
        if (buff == null) {
            return ByteBuffer.allocateDirect(BUFFER_ALLOCATION_SIZE);
        }
        buff.clear();
        return buff;
    }
    private static void releaseBuffer(ByteBuffer buff) {
        BUFFER_POOL.add(buff.clear());
    }}