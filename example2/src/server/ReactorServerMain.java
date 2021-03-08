package server;

import clientServerSharedModel.NewsFeed;
import clientServerSharedModel.NewsFeedImpl;
import encoderDecoder.ObjectEncoderDecoder;
import protocol.RemoteCommandInvocationProtocol;
import reactor.Reactor;

public class ReactorServerMain {
    public static void main(String[] args) {
        NewsFeed feed = new NewsFeedImpl(); //one client/server shared object
        new Reactor<>(20,
                7777,
                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                ObjectEncoderDecoder::new
        ).serve();
    }
}
