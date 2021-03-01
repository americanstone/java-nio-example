package client;

import server.NewsFeed;

import java.io.Serializable;

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