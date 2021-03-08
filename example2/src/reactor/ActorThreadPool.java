package reactor;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/*
you can think of the actor thread pool as if it run actions of actors in a play - one can submit new actions for actors
and the pool will make sure that each actor will run its actions in the order they were received while not blocking other
 threads.
 */
public class ActorThreadPool<T> {
    //hold the task queues of the actors. An entry in a WeakHashMap will
    //automatically be removed when its key is no longer in ordinary use
    private final Map<T, Queue<Runnable>> acts;
    //Like most collection classes, this class is not synchronized. And therefore we will guard access to it using the read-write lock: actsRWLock
    private final ReadWriteLock actsRWLock;
    //Internally, it uses a simple fixed executor service but in order to not add two task of the same act to the pool it maintain the playingNow set.
    private final Set<T> playingNow;

    private final ExecutorService threads;

    public ActorThreadPool(int threads) {
        this.threads = Executors.newFixedThreadPool(threads);
        acts = new WeakHashMap<>();
        playingNow = ConcurrentHashMap.newKeySet();
        actsRWLock = new ReentrantReadWriteLock();
    }

    public void submit(T act, Runnable job) {
        synchronized (act) {
            if (!playingNow.contains(act)) {
                playingNow.add(act);
                execute(job, act); // return immediately, another job submit may update the job for same act
            } else {
                pendingRunnablesOf(act).add(job);
            }
        }
    }

    private Queue<Runnable> pendingRunnablesOf(T act) {
        actsRWLock.readLock().lock();
        Queue<Runnable> pendingRunnables = acts.get(act);
        actsRWLock.readLock().unlock();

        if (pendingRunnables == null) {
            actsRWLock.writeLock().lock();
            acts.put(act, pendingRunnables = new LinkedList<>());
            actsRWLock.writeLock().unlock();
        }
        return pendingRunnables;
    }

    private void execute(Runnable r, T act) {
        threads.submit(() -> {
            try {
                r.run();
            } finally {
                complete(act); //
            }
        });
    }

    //method that is blocking and is executed by the pool threads is the complete method which acquire the
    //act monitor but only for a very short amount of time.
    private void complete(T act) {
        synchronized (act) {
            Queue<Runnable> pending = pendingRunnablesOf(act);
            if (pending.isEmpty()) {
                playingNow.remove(act);
            } else {
                execute(pending.poll(), act);
            }
        }
    }

    public void shutdown() {
        threads.shutdownNow();
    }
}