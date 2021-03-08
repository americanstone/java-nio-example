package server;

import encoderDecoder.MessageEncoderDecoder;
import handler.ConnectionHandler;
import protocol.MessagingProtocol;

import java.util.function.Supplier;

public class SingleThreadedServer extends BaseServer {

    public SingleThreadedServer(
            int port,
            Supplier<MessagingProtocol> protocolFactory,
            Supplier<MessageEncoderDecoder> encoderDecoderFactory) {

        super(port,protocolFactory,encoderDecoderFactory);
    }

    @Override
    protected void execute(ConnectionHandler handler) {
        handler.run();
    }

}