package protocol;

import client.Command;

import java.io.Serializable;

public class RemoteCommandInvocationProtocol<T> implements MessagingProtocol<Serializable> {

    private final T data;

    public RemoteCommandInvocationProtocol(T data) {
        this.data = data;
    }

    @Override
    public Serializable process(Serializable msg) {
        return ((Command) msg).execute(data);
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

}