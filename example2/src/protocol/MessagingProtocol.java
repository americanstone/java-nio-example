package protocol;

/*
        The task of the MessagingProtocol is to look at
        the message and decide what should be done. This decision may depend on the state of the connection
        (remember the example of the "authenticated" protocol).
        Once the action is performed, we will need to send an answer to the client. So we expect to get an answer back from
        the MessagingProtocol.
 */
public interface MessagingProtocol<T> {

    /**
     * process the given message
     * @param msg the received message
     * @return the response to send or null if no response is expected by the client
     */
    T process(T msg);

    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();

}