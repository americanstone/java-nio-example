package server;

import protocol.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class FixedThreadPoolServer extends BaseServer {

    private final ExecutorService pool;

    public FixedThreadPoolServer(
            int numThreads,
            int port,
            Supplier<MessagingProtocol> protocolFactory,
            Supplier<MessageEncoderDecoder> encoderDecoderFactory) {

        super(port, protocolFactory, encoderDecoderFactory);
        this.pool = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    public void serve() {
        super.serve();
        pool.shutdown();
    }

    @Override
    protected void execute(ConnectionHandler handler) {
        pool.execute(handler);
    }
}