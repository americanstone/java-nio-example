package client;

import server.NewsFeed;

import java.io.Serializable;

public class PublishNewsCommand implements Command<NewsFeed> {

    private String category;
    private String news;

    public PublishNewsCommand(String category, String news) {
        this.category= category;
        this.news = news;
    }

    @Override
    public Serializable execute(NewsFeed feed) {
        feed.publish(category, news);
        return "OK";
    }

}