package clientServerSharedModel;

import java.util.List;

public interface NewsFeed {

    void clear();

    List<String> fetch(String category);

    void publish(String category, String news);

}