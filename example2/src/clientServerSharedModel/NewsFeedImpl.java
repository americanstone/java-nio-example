package clientServerSharedModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NewsFeedImpl implements NewsFeed {

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> newsPerCategory = new ConcurrentHashMap<>();

    @Override
    public List<String> fetch(String category) {
        ConcurrentLinkedQueue<String> queue = newsPerCategory.get(category);
        if (queue == null) {
            return new ArrayList<>(0); //empty
        } else {
            return new ArrayList<>(queue); //copy of the queue, arraylist is serializable
        }
    }

    @Override
    public void publish(String category, String news) {
        ConcurrentLinkedQueue<String> queue = newsPerCategory.computeIfAbsent(category, (k) -> new ConcurrentLinkedQueue<>());
        queue.add(news);
    }

    @Override
    public void clear() {
        newsPerCategory.clear();
    }
}