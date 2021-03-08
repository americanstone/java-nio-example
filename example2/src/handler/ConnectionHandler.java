package handler;

import encoderDecoder.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
The ConnectionHandler object maintains the state of the connection for the specific client which it serves
(for example, if the user performed "login", the ConnectionHandler object will remember this in its state).
 The ConnectionHandler also has access to the Socket connecting the server to the client process.
 */
public class ConnectionHandler<T> implements Runnable {
    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket clientSocket; //references to the TCP socket connected to the client

    public ConnectionHandler(Socket clientSocket, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol) {
        this.clientSocket = clientSocket;
        this.encdec = reader;
        this.protocol = protocol;
    }

    // server can run it in different ways single thread, pre-thread or threadpool
    /*
        A ConnectionHandler instance wraps together: the socket connected to the client;
        the MessageEncoderDecoder which splits incoming bytes from the socket into messages.
        The next step is to pass the incoming messages from the client to the MessagingProtocol
        which will now execute the action requested by the client. The task of the MessagingProtocol is to look at
        the message and decide what should be done. This decision may depend on the state of the connection
        (remember the example of the "authenticated" protocol).
        Once the action is performed, we will need to send an answer to the client. So we expect to get an answer back from
        the MessagingProtocol.
     */
    @Override
    public void run() {
        try (Socket sock = this.clientSocket; //just for automatic closing
             BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {

            int read;
            while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    // execute the action requested by the client
                    T response = protocol.process(nextMessage);
                    if (response != null) {
                        out.write(encdec.encode(response));
                        out.flush();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}