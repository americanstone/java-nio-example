package protocol;

import clientServerSharedModel.Command;

import java.io.Serializable;

/*
    server side hold the data, client command can execute on
 */
public class RemoteCommandInvocationProtocol<T> implements MessagingProtocol<Serializable> {

    private final T data;

    public RemoteCommandInvocationProtocol(T data) {
        this.data = data;
    }

    /*
        hwo to manipulate the data up to the commands client passed in
     */
    @Override
    public Serializable process(Serializable msg) {
        return ((Command) msg).execute(data);
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}