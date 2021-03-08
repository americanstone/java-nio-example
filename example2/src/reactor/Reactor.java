package reactor;

import encoderDecoder.MessageEncoderDecoder;
import protocol.MessagingProtocol;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
/*
    Reactor is an active object. @see https://www.wikiwand.com/en/Active_object
    It is the heart of the architecture which connects the other components and triggers their operation.
    The key components of the Reactor are:
    The selector
    The thread pool executor
    The Reactor thread listens to events from the selector. Initially, only a ServerSocketChannel is connected to the selector.
    The Reactor can, therefore, only react to accept events.
    The serve() method of the Reactor dispatches the events it receives from the selector, and reacts appropriately.
 */
public class Reactor<T> implements Closeable {
    private final int port;
    private final Supplier<MessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> readerFactory;
    private final ActorThreadPool<NonBlockingConnectionHandler<T>> pool;
    private Selector selector;
    private Thread selectorThread; // main thread

    private final ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();

    public Reactor(
            int numThreads,
            int port,
            Supplier<MessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> readerFactory) {

        this.pool = new ActorThreadPool<>(numThreads);
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.readerFactory = readerFactory;
    }

    public void serve() {
        System.out.println("Server listen on " + port);
        selectorThread = Thread.currentThread();
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSockChan = ServerSocketChannel.open()) {

            this.selector = selector; //just to be able to close

            serverSockChan.bind(new InetSocketAddress(port));
            serverSockChan.configureBlocking(false);

            // init binding server socket with selector and only interested ACCEPT
            serverSockChan.register(selector, SelectionKey.OP_ACCEPT);
            
            // event loop dispatch the I/O work to handler
            // by asking selector which channels have event and dispatch them
            while (!selectorThread.isInterrupted()) {
                //Selects a set of keys whose corresponding channels are ready for I/O operations.
                //This method performs a blocking selection operation. put in sleep if no event and give up on cpu
                int numberOfKeys = selector.select();

                // this is necessary to prevent the same key from coming up again the next time around.
                runSelectionThreadTasks();

                //Selectors tell which of a set of Channels has IO events.
                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        // in main thread no blocking call only register
                        handleAccept(serverSockChan, selector);
                    } else {
                        // will be ran in different thread
                        handleReadWrite(key);
                    }
                }
                selector.selectedKeys().clear(); //clear the selected keys set so that we can know about new events
            }
        } catch (ClosedSelectorException ex) { //do nothing - server was requested to be closed
        } catch (IOException ex) {             //this is an error
            ex.printStackTrace();
        }
        System.out.println("server closed!!!");
        pool.shutdown();
    }

    /*
    the handleAccept method is invoked by the Reactor main thread each time the ServerSocketChannel becomes acceptable.
    accept() obtains a SocketChannel from the ServerSocketChannel, and connects it to the selector. It then creates a
    NonBlockingConnectionHandler passive object to keep track of the state of the newly created connection.
    Finally, register the channel to the selector with OP_READ and the NonBlockingConnectionHandler attached to the selection key.
    This way, when the selector triggers an event on this channel, we will find easily the corresponding NonBlockingConnectionHandler
    object that keeps track of its current state.
    Passive objects in UML do not generally have the ability to modify or begin the flow of execution,
    because they must wait for another object to call them. Instead, passive objects are generally used to store information,
    and in many cases this information may be shared between multiple other objects
     */
    private void handleAccept(ServerSocketChannel serverChanNonBlocking, Selector selector) throws IOException {
        System.out.println("handle accept!");

        SocketChannel clientChan = serverChanNonBlocking.accept();
        clientChan.configureBlocking(false);
        /*
            the client channel connect to socket corresponding to each client
            can be made from a single java.nio.channels.ServerSocketChannel which is bound to a single port.
        */
        SocketAddress remoteAddress = clientChan.getRemoteAddress();
        System.out.println("Server connected to client: " + remoteAddress);

        // the handler also hold the client stock reference
        final NonBlockingConnectionHandler handler = new NonBlockingConnectionHandler(
                readerFactory.get(),
                protocolFactory.get(),
                clientChan, // the channel connect to client socket Socket clientSocket = clientChan.socket();
                this);
        // connect the client socket to selector(hold the event selectionKeys), which OP interested from the selector and the handler to dealer it
        // the same handle will be retrieved via selector's SelectionKey attachment
        clientChan.register(selector, SelectionKey.OP_READ, handler);
    }

    private void handleReadWrite(SelectionKey key) {
        System.out.println("handle RW!");
        // the handler was attached in handleAccept method
        NonBlockingConnectionHandler handler = (NonBlockingConnectionHandler) key.attachment();
        if (key.isReadable()) {
            // called from the selector thread the continueRead method can also return a Runnable that represents the
            // protocol-related task that was created in response to the read event and should be executed in a worker thread.
            Runnable task = handler.continueRead();
            if (task != null) { // read data from client stock didn't success
                // maintain the task execution order is the key
                pool.submit(handler, task); // the reason to pass handler to the worker threadpool is for thread coordination
                /*
                 w/o synchronize on handler, We can see that there may be a situation where two different threads are performing the tasks of the same connection
                 - why this is a problem?Assume a client that send two messages M1 and M2 to the server.
                 The server then, create two tasks T1 and T2 corresponding to the messages.Since two different
                 threads may handle the task concurrently, it may happen that T2 will be completed before T1.
                 This behavior will result insending the response to M2 before the response to M1.
                 This will most probably going to break our protocol.
                 */
            }
        } else {
            handler.continueWrite();
        }
    }

    private void runSelectionThreadTasks() {
        while (!selectorTasks.isEmpty()) {
            selectorTasks.remove().run();
        }
    }

    // called from same or different thread(s) tell the selection to update SelectionKey's interestOps
    // which alredy bind to a channel
    void updateInterestedOps(SocketChannel chan, int ops) {
        final SelectionKey key = chan.keyFor(selector);
        if (Thread.currentThread() == selectorThread) { // main thread
            key.interestOps(ops);
        } else {
            selectorTasks.add(() -> {
                if(key.isValid())
                    key.interestOps(ops);
            });
            selector.wakeup();
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }
}