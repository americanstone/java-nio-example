package client;

import clientServerSharedModel.Command;
import clientServerSharedModel.NewsFeed;

import java.io.Serializable;
/*
    object will be passed to server side to execute the NewsFeed
 */
public class FetchNewsCommand implements Command<NewsFeed> {

    private String category;

    public FetchNewsCommand(String category) {
        this.category= category;
    }

    @Override
    public Serializable execute(NewsFeed feed) {
        return (Serializable) feed.fetch(category);
    }
}