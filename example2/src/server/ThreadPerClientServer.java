package server;

import protocol.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.util.function.Supplier;

public class ThreadPerClientServer extends BaseServer {

    public ThreadPerClientServer(
            int port,
            Supplier<MessagingProtocol> protocolFactory,
            Supplier<MessageEncoderDecoder> encoderDecoderFactory) {

        super(port, protocolFactory, encoderDecoderFactory);
    }

    @Override
    protected void execute(ConnectionHandler handler) {
        new Thread(handler).start();
    }

}