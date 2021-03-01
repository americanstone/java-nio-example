package server;

import protocol.ObjectEncoderDecoder;
import protocol.RemoteCommandInvocationProtocol;

public class NewsFeedServerMain {
    public static void main(String[] args) {
        NewsFeed feed = new NewsFeedImpl(); //one shared object

        new ThreadPerClientServer(
                7777, //port
                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                () -> new ObjectEncoderDecoder<>()                 //message encoder decoder factory
        ).serve();
    }
}
