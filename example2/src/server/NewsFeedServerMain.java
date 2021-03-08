package server;

import clientServerSharedModel.NewsFeed;
import clientServerSharedModel.NewsFeedImpl;
import encoderDecoder.ObjectEncoderDecoder;
import protocol.RemoteCommandInvocationProtocol;

/*
     This server will allow clients to execute two commands:
     1. Publish news to a category by its name
     2. Fetch all the news which were published to a specific category
 */
public class NewsFeedServerMain {
    public static void main(String[] args) {
        //Note that the client works with the interface of news feed while the server will have the actual implementation.
        NewsFeed feed = new NewsFeedImpl(); //one client/server shared object

        new ThreadPerClientServer(
                7777, //port
                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                ObjectEncoderDecoder::new                 //message encoder decoder factory
        ).serve();
    }
}
