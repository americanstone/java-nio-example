package server;

import protocol.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer {

    private final int port;
    private final Supplier<MessagingProtocol> protocolFactory;
    private final Supplier<MessageEncoderDecoder> encdecFactory;

    public BaseServer(
            int port,
            Supplier<MessagingProtocol> protocolFactory,
            Supplier<MessageEncoderDecoder> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
    }

    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                ConnectionHandler handler = new ConnectionHandler(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());

                execute(handler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("server closed!!!");
    }


    protected abstract void execute(ConnectionHandler handler);
}